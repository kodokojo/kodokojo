package io.kodokojo.service.dns;

import java.util.List;

public interface DnsManager {

    boolean dnsEntryExist(DnsEntry dnsEntry);

    List<DnsEntry> getDnsEntries(String name);

    boolean createDnsEntry(DnsEntry dnsEntry);

}
