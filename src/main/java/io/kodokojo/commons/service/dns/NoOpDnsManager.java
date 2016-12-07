/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.service.dns;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    @Override
    public void createOrUpdateDnsEntries(Set<DnsEntry> dnsEntries) {
        //Nothing to do.
    }
}
