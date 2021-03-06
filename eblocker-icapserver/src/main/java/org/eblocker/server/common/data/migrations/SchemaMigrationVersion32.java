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

import org.eblocker.server.common.data.UserModuleOld;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eblocker.server.common.data.dashboard.DashboardCard;
import org.eblocker.server.common.data.DataSource;
import org.eblocker.server.http.service.DashboardService;
import org.eblocker.registration.ProductFeature;
import com.google.inject.Inject;

public class SchemaMigrationVersion32 implements SchemaMigration {
    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationVersion32.class);

    private final DataSource dataSource;
    private final DashboardService dashboardService;
    private final UserMigrationService userMigrationService;

    @Inject
    public SchemaMigrationVersion32(DataSource dataSource, DashboardService dashboardService,
                                    UserMigrationService userMigrationService) {
        this.dataSource = dataSource;
        this.dashboardService = dashboardService;
        this.userMigrationService = userMigrationService;
    }

    @Override
    public String getSourceVersion() {
        return "31";
    }

    @Override
    public String getTargetVersion() {
        return "32";
    }

    @Override
    public void migrate() {
        List<UserModuleOld> users = userMigrationService.getAll();
        int eblMobileCardId = dashboardService.generateEblockerMobileCard().getId();

        for (UserModuleOld user : users) {
            // Get the eBlocker Mobile-card
            Optional<DashboardCard> eblMobileCard = user.getDashboardCards().stream()
                    .filter(c -> c.getId() == eblMobileCardId).findFirst();
            if (!eblMobileCard.isPresent()) {
                // error - the user should have such a card
                // But the DashbordService will make sure that she gets one!
            } else {
                DashboardCard card = eblMobileCard.get();
                if (!ProductFeature.BAS.name().equalsIgnoreCase(card.getRequiredFeature())) {
                    // Must change required feature

                    // New card with required feature updated
                    DashboardCard newCard = new DashboardCard(card.getId(), ProductFeature.BAS.name(), card.getTranslateSuffix(),
                            card.getHtml(), card.isVisible(), card.isAlwaysVisible(), card.getDefaultPos(),
                            card.getCustomPos());

                    // Keep every card but replace the eBlocker Mobile Card
                    user.setDashboardCards(user.getDashboardCards().stream()
                            .map(c -> c.getId() == newCard.getId() ? newCard : c).collect(Collectors.toList()));
                    userMigrationService.save(user, user.getId());
                }
            }
        }
        dataSource.setVersion("32");
    }

}
