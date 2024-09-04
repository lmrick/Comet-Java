package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.vouchers.IVoucher;
import com.cometproject.api.game.catalog.types.vouchers.VoucherStatus;
import com.cometproject.api.game.catalog.types.vouchers.VoucherType;

public record Voucher(int id, VoucherType type, String data, int createdBy, int createdAt, int claimedBy, int claimedAt,
                      VoucherStatus status, String code) implements IVoucher {
	
	
	
}
