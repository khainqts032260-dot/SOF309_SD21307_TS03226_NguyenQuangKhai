package vn.celineclosset.payment;

import java.math.BigDecimal;

/** Dữ liệu giao dịch tiền vào do SePay gửi tới webhook. */
public class SePayWebhookPayload {
    private long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String code;
    private String content;
    private String transferType;
    private String description;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String referenceCode;

    public long getId() { return id; }
    public String getGateway() { return gateway; }
    public String getTransactionDate() { return transactionDate; }
    public String getAccountNumber() { return accountNumber; }
    public String getSubAccount() { return subAccount; }
    public String getCode() { return code; }
    public String getContent() { return content; }
    public String getTransferType() { return transferType; }
    public String getDescription() { return description; }
    public BigDecimal getTransferAmount() { return transferAmount; }
    public BigDecimal getAccumulated() { return accumulated; }
    public String getReferenceCode() { return referenceCode; }
}
