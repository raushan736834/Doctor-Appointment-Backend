package com.harsh.AppointDoctor.Utility;

import java.util.concurrent.ThreadLocalRandom;

public class OtpGenerator {
    public static int generateSixDigitOtp() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
