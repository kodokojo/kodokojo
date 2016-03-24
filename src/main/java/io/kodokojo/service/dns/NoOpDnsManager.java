package io.kodokojo.service.dns;

import java.util.Collections;
import java.util.List;

public class NoOpDnsManager implements DnsManager {
    @Override
    public boolean dnsEntryExist(DnsEntry dnsEntry) {
        return false;
    }

    @Override
    public List<DnsEntry> getDnsEntries(String name) {
        return Collections.emptyList();
    }

    @Override
    public boolean createOrUpdateDnsEntry(DnsEntry dnsEntry) {
        return false;
    }
}
