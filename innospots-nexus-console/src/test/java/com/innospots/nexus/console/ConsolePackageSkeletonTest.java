package com.innospots.nexus.console;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsolePackageSkeletonTest {

    @Test
    void consoleDeclaresPhaseTwoPackageRoots() throws ClassNotFoundException {
        String[] packages = {
                "com.innospots.nexus.console.auth",
                "com.innospots.nexus.console.credential",
                "com.innospots.nexus.console.role",
                "com.innospots.nexus.console.menu",
                "com.innospots.nexus.console.permission",
                "com.innospots.nexus.console.extension",
                "com.innospots.nexus.console.logger",
                "com.innospots.nexus.console.dictionary"
        };
        for (String name : packages) {
            assertThat(Class.forName(name + ".package-info")).isNotNull();
        }
    }
}
