package io.kodokojo.service.dns;

import java.util.List;
import java.util.Set;

public interface DnsManager {

    boolean dnsEntryExist(DnsEntry dnsEntry);

    List<DnsEntry> getDnsEntries(String name);

    boolean createOrUpdateDnsEntry(DnsEntry dnsEntry);

    void createOrUpdateDnsEntries(Set<DnsEntry> dnsEntries);
}
