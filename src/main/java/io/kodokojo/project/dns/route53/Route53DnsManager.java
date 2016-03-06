package io.kodokojo.project.dns.route53;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.*;
import io.kodokojo.project.dns.DnsEntry;
import io.kodokojo.project.dns.DnsManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class Route53DnsManager implements DnsManager {

    private final String domainName;

    private final AmazonRoute53Client client;

    public Route53DnsManager(String domainName, Region region) {
        if (isBlank(domainName)) {
            throw new IllegalArgumentException("domainName must be defined.");
        }
        this.domainName = domainName.endsWith(".") ? domainName : domainName + ".";

        AWSCredentials credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
        client = new AmazonRoute53Client(credentials);
        client.setRegion(region == null ? Region.getRegion(Regions.EU_WEST_1) : region);
    }

    @Override
    public boolean createDnsEntry(DnsEntry dnsEntry) {
        if (dnsEntry == null) {
            throw new IllegalArgumentException("dnsEntry must be defined.");
        }
        HostedZone hostedZone = getHostedZone();
        if (hostedZone != null) {
            List<ResourceRecord> resourceRecords = new ArrayList<>();
            ResourceRecord resourceRecord = new ResourceRecord();
            String value = dnsEntry.getValue();
            resourceRecord.setValue((dnsEntry.getType().equals(DnsEntry.Type.CNAME) ? valideDnsName(value) : value));
            resourceRecords.add(resourceRecord);

            ResourceRecordSet resourceRecordSet = new ResourceRecordSet();
            resourceRecordSet.setName(valideDnsName(dnsEntry.getName()));
            resourceRecordSet.setType(RRType.valueOf(dnsEntry.getType().toString()));
            resourceRecordSet.setTTL(300L);

            resourceRecordSet.setResourceRecords(resourceRecords);

            List<Change> changes = new ArrayList<>();
            Change change = new Change();
            change.setAction(ChangeAction.CREATE);
            change.setResourceRecordSet(resourceRecordSet);
            changes.add(change);

            ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
            ChangeBatch changeBatch = new ChangeBatch();
            changeBatch.setChanges(changes);
            request.setChangeBatch(changeBatch);
            request.setHostedZoneId(getHostedZoneID(hostedZone));
            //ChangeResourceRecordSetsResult result =
            client.changeResourceRecordSets(request);
            return true;
        }
        return false;
    }

    @Override
    public boolean dnsEntryExist(DnsEntry dnsEntry) {
        if (dnsEntry == null) {
            throw new IllegalArgumentException("dnsEntry must be defined.");
        }
        HostedZone hostedZone = getHostedZone();
        boolean found = false;
        if (hostedZone != null) {
            Iterator<ResourceRecordSet> iterator = getResourceRecordSet(hostedZone).iterator();
            String dnsEntryName = valideDnsName(dnsEntry.getName());
            while (!found && iterator.hasNext()) {
                ResourceRecordSet recordSet = iterator.next();
                found = recordSet.getName().equals(dnsEntryName)
                        && DnsEntry.Type.valueOf(recordSet.getType()).equals(dnsEntry.getType());
            }
        }
        return found;
    }


    @Override
    public List<DnsEntry> getDnsEntries(String name) {
        String dnsName = null;
        if (isNotBlank(name)) {
            dnsName = valideDnsName(name);
        }
        HostedZone hostedZone = getHostedZone();
        List<DnsEntry> res = new ArrayList<>();
        if (hostedZone != null) {
            List<ResourceRecordSet> resourceRecordSet = getResourceRecordSet(hostedZone);
            for (ResourceRecordSet recordSet : resourceRecordSet) {
                if ((isNotBlank(dnsName) && recordSet.getName().equals(dnsName)) || isBlank(dnsName)) {
                    DnsEntry dnsEntry = convertDnsEnrty(recordSet);
                    res.add(dnsEntry);
                }
            }
        }
        return res;
    }


    private HostedZone getHostedZone() {
        ListHostedZonesByNameRequest listHostedZonesByNameRequest = new ListHostedZonesByNameRequest();
        listHostedZonesByNameRequest.setDNSName(domainName);
        ListHostedZonesByNameResult result = client.listHostedZonesByName(listHostedZonesByNameRequest);

        Iterator<HostedZone> iterator = result.getHostedZones().iterator();
        HostedZone hostedZone = null;
        while (hostedZone == null && iterator.hasNext()) {
            HostedZone currentZone = iterator.next();
            hostedZone = currentZone.getName().equals(domainName) ? currentZone : null;
        }
        return hostedZone;
    }

    private List<ResourceRecordSet> getResourceRecordSet(HostedZone hostedZone) {
        ListResourceRecordSetsRequest listResourceRecordSetsRequest = new ListResourceRecordSetsRequest();
        listResourceRecordSetsRequest.setHostedZoneId(getHostedZoneID(hostedZone));
        ListResourceRecordSetsResult recordSetsResult = client.listResourceRecordSets(listResourceRecordSetsRequest);
        return recordSetsResult.getResourceRecordSets();
    }

    private static String valideDnsName(String name) {
        return name.endsWith(".") ? name : name + ".";
    }

    private static String getHostedZoneID(HostedZone hostedZone) {
        return hostedZone.getId().substring("/hostedzone/".length());
    }


    private static DnsEntry convertDnsEnrty(ResourceRecordSet resourceRecordSet) {
        String dnsName = resourceRecordSet.getName();
        DnsEntry.Type dnsType = DnsEntry.Type.valueOf(resourceRecordSet.getType());
        String value = resourceRecordSet.getResourceRecords().get(0).getValue();
        return new DnsEntry(dnsName, dnsType, value);
    }
}


