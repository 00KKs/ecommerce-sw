package github.sangwook.ecommerce.integration.payment;

import github.sangwook.ecommerce.payment.infrastructure.PaymentLookupResult;

import java.time.OffsetDateTime;

public enum LookupScenario {
    DONE {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.DONE(stored.getAmount(), OffsetDateTime.now());
        }
    },
    DONE_AMOUNT_MISMATCH {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.DONE(stored.getAmount() + 1, OffsetDateTime.now());
        }
    },
    READY {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.READY();
        }
    },
    ABORTED {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.ABORTED();
        }
    },
    NOT_FOUND {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.NOT_FOUND();
        }
    },
    UNAVAILABLE {
        @Override
        PaymentLookupResult respond(FakePgPayment stored) {
            return new PaymentLookupResult.UNAVAILABLE("IO ERROR");
        }
    };

      abstract PaymentLookupResult respond(FakePgPayment stored);
  }