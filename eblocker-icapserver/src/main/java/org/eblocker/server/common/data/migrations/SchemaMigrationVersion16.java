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
package org.eblocker.server.common.data.migrations;

import org.eblocker.server.common.data.DataSource;
import org.eblocker.server.common.data.Device;
import org.eblocker.server.common.data.messagecenter.MessageCenterMessage;
import org.eblocker.server.common.data.messagecenter.MessageContainer;
import org.eblocker.server.common.data.messagecenter.MessageSeverity;
import org.eblocker.server.common.data.messagecenter.provider.MessageProviderMessageId;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;

/**
 * Has been a migration to create EblockerDnsServerState based on current config but that is obsolete now. In the meantime this migration has been captured by a merge :(
 */
public class SchemaMigrationVersion16 implements SchemaMigration {

    private final DataSource dataSource;

    private static final List<Integer> ALERT_MESSAGE_IDS = Arrays.asList(
        MessageProviderMessageId.MESSAGE_LICENSE_EXPIRING_ID.getId(),
        MessageProviderMessageId.MESSAGE_LICENSE_EXPIRED_ID.getId(),
        MessageProviderMessageId.MESSAGE_CERTIFICATE_EXPIRATION_WARNING.getId(),
        MessageProviderMessageId.MESSAGE_CERTIFICATE_UNTRUSTED_WARNING.getId()
    );

    @Inject
    public SchemaMigrationVersion16(
        DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getSourceVersion() {
        return "15";
    }

    @Override
    public String getTargetVersion() {
        return "16";
    }

    @Override
    public void migrate() {
        List<MessageContainer> messageContainers = dataSource.getAll(MessageContainer.class);
        for (MessageContainer messageContainer: messageContainers) {
            if (messageContainer.getMessage() != null) {
                MessageCenterMessage message = messageContainer.getMessage();
                int id = message.getId();
                if (ALERT_MESSAGE_IDS.contains(id)) {
                    messageContainer = new MessageContainer(
                        new MessageCenterMessage(
                            message.getId(),
                            message.getTitleKey(),
                            message.getContentKey(),
                            message.getActionButtonLabelKey(),
                            message.getActionButtonUrlKey(),
                            message.getTitles(),
                            message.getContents(),
                            message.getContext(),
                            message.getDate(),
                            message.isShowDoNotShowAgain(),
                            MessageSeverity.ALERT
                        ),
                        messageContainer.getVisibility()
                    );
                    dataSource.save(messageContainer, id);
                }
            }
        }

        setMessageSeverityForDevices();

        dataSource.setVersion("16");
    }

    private void setMessageSeverityForDevices() {
        for (Device device : dataSource.getDevices()) {
            device.setMessageShowInfo(true);
            device.setMessageShowAlert(true);
            dataSource.save(device);
        }
    }

}
