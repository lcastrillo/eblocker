/*
 * Copyright 2020 eBlocker Open Source UG (haftungsbeschraenkt)
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the EUPL
 * (the "License"); You may not use this work except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 *   https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.eblocker.server.common.squid.acl;

import org.eblocker.server.common.data.Device;
import org.eblocker.server.common.data.IpAddress;
import org.eblocker.server.http.service.DeviceService;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class DeviceFilterAcl extends SquidAcl {

    private final DeviceService deviceService;

    DeviceFilterAcl(String path, DeviceService deviceService) {
        super(path);
        this.deviceService = deviceService;
    }

    @Override
    protected final Set<String> getAclEntries() {
        return deviceService.getDevices(false).stream()
            .filter(this::filter)
            .flatMap(this::ipAddresses)
            .collect(Collectors.toSet());
    }

    protected abstract boolean filter(Device device);

    protected Stream<String> ipAddresses(Device device) {
        return device.getIpAddresses().stream().map(IpAddress::toString);
    }
}
