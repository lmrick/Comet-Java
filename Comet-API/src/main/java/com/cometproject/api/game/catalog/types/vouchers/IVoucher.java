package com.cometproject.api.game.catalog.types.vouchers;

public interface IVoucher {
    int id();

    VoucherType type();

    String data();

    int createdBy();

    int createdAt();

    int claimedBy();

    int claimedAt();

    VoucherStatus status();

    String code();
}
