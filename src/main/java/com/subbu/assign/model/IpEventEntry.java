package com.subbu.assign.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.subbu.assign.events.IpEventOuterClass.IpEvent;

import java.util.HashSet;
import java.util.Set;

public class IpEventEntry {
    @JsonIgnore
    private String appSha256;
    @JsonProperty("count")
    private long count;
    @JsonIgnore
    private long network;
    @JsonProperty("good_ips")
    private Set<String> goodIps;
    @JsonProperty("bad_ips")
    private Set<String> badIps;

    public IpEventEntry(IpEvent ipEvent) {
        this.appSha256 = ipEvent.getAppSha256();
        this.count = 0;
        this.network = ipEvent.getIp() & 0xFFFFFFF0;
        this.goodIps = new HashSet<String>();
        this.badIps = new HashSet<String>();
    }

    public String getAppSha256() {
        return appSha256;
    }

    public void setAppSha256(String appSha256) {
        this.appSha256 = appSha256;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long counts) {
        this.count = counts;
    }

    public void incrementCount() {
        this.count++;
    }

    public long getNetwork() {
        return network;
    }

    public void setNetwork(long network) {
        this.network = network;
    }

    public Set<String> getGoodIps() {
        return goodIps;
    }

    public void setGoodIps(Set<String> goodIps) {
        this.goodIps = goodIps;
    }

    public Set<String> getBadIps() {
        return badIps;
    }

    public void setBadIps(Set<String> badIps) {
        this.badIps = badIps;
    }
}
