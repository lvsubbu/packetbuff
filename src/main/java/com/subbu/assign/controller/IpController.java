package com.subbu.assign.controller;

import com.subbu.assign.events.IpEventOuterClass.IpEvent;
import com.subbu.assign.model.IpEventEntry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.concurrent.*;


@RestController
public class IpController {

    private ConcurrentMap<String, IpEventEntry> ipEventEntries = new ConcurrentHashMap<>();
    private BlockingQueue<IpEvent> ipEventQueue = new ArrayBlockingQueue<>(10);
    private ThreadPoolExecutor ipEventExecutor = new ThreadPoolExecutor(20, 100, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    @RequestMapping(value = "/events/{app_sha256}", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<IpEventEntry> getEvents(@Valid @NotNull @PathVariable("app_sha256") String appSha256) {
        HttpStatus httpStatus = HttpStatus.OK;
        IpEventEntry ipEventEntry = ipEventEntries.get(appSha256);
        if (ipEventEntry == null) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(ipEventEntry, httpStatus);
    }

    @RequestMapping(value = "/events", consumes = "application/octet-stream", method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> handleEvents(InputStream protobufStream) {
        try {
            IpEvent ipEvent = IpEvent.parseFrom(protobufStream);
            ipEventQueue.put(ipEvent);
            ipEventExecutor.execute(new IpEventRunner());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/events", method = RequestMethod.DELETE)
    public ResponseEntity<HttpStatus> deleteEvents() {
        ipEventEntries.clear();
        ipEventQueue.clear();
        ipEventExecutor.purge();
        return new ResponseEntity<HttpStatus>(HttpStatus.OK);
    }

    public class IpEventRunner implements Runnable {

        @Override
        public void run() {
            try {
                IpEvent ipEvent = ipEventQueue.take();
                String appSha256 = ipEvent.getAppSha256();

                IpEventEntry ipEventEntry = ipEventEntries.get(appSha256);
                if (ipEventEntry == null) {
                    ipEventEntry = new IpEventEntry(ipEvent);
                    ipEventEntries.put(appSha256,ipEventEntry);
                }

                long ip = ipEvent.getIp();
                long network = ipEventEntry.getNetwork();
                String ipStr = String.format("%d.%d.%d.%d", (ip >> 24 & 0xff), (ip >> 16 & 0xff), (ip >> 8 & 0xff), (ip & 0xff));

                if (Math.abs(network - ip) < 28) {
                    ipEventEntry.getGoodIps().add(ipStr);
                } else {
                    ipEventEntry.getBadIps().add(ipStr);
                }
                ipEventEntry.incrementCount();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}

