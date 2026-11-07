package vn.celineclosset.payment;

/** Kết quả nội bộ sau khi đối chiếu một giao dịch SePay. */
public record PaymentResult(
        boolean accepted,
        boolean duplicate,
        Integer orderId,
        String paymentStatus,
        String reconciliationStatus,
        String message
) {
}
