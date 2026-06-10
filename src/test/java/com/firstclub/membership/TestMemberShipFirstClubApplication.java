package com.firstclub.membership;

import org.springframework.boot.SpringApplication;

public class TestMemberShipFirstClubApplication {

    public static void main(String[] args) {
        SpringApplication.from(MemberShipFirstClubApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
