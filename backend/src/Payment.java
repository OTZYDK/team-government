public class Payment {

    public enum Method {
        CASH, CREDIT
    }

    private Method method;

    private long amountPaidCents;
    private long changeDueCents;

    private String cardLastFour;
    private String message;

    // Method to process cash payment
    public String payCash(double totalDue, double cashReceived) {

        resetPayment();

        try {
            long total = toCents(totalDue);
            long received = toCents(cashReceived);

            if (received < total) {
                message = "DECLINED: Insufficient cash. Amount still due: $"
                        + formatMoney(total - received);
                return message;
            }

            method = Method.CASH;
            amountPaidCents = received;
            changeDueCents = received - total;

            message = "ACCEPTED: Cash payment successful. Change due: $"
                    + formatMoney(changeDueCents);

        } catch (IllegalArgumentException e) {
            message = "DECLINED: Invalid payment amount.";
        }

        return message;
    }

    // Method to process credit payment
    public String payCredit(double totalDue, String cardNumber) {

        resetPayment();

        if (!Double.isFinite(totalDue) || totalDue < 0) {
            message = "DECLINED: Invalid transaction total.";
            return message;
        }

        if (cardNumber == null || cardNumber.isBlank()) {
            message = "DECLINED: No card number provided.";
            return message;
        }

        String number = cardNumber.replaceAll("[ -]", "");

        if (!number.matches("4\\d{15}")) {
            message = "DECLINED: Card must start with 4 and contain exactly 16 digits.";
            return message;
        }

        method = Method.CREDIT;
        cardLastFour = number.substring(12);

        amountPaidCents = toCents(totalDue);
        changeDueCents = 0;

        message = "ACCEPTED: Credit payment successful. Card ending in "
                + cardLastFour + ".";

        return message;
    }

    private long toCents(double amount) {

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        return Math.round(amount * 100);
    }

    private String formatMoney(long cents) {
        return String.format("%.2f", cents / 100.0);
    }

    private void resetPayment() {
        method = null;
        cardLastFour = null;
        amountPaidCents = 0;
        changeDueCents = 0;
        message = null;
    }

    public Method getMethod() {
        return method;
    }

    public String getMethodString() 
    {
    	if (method == Method.CREDIT)
    	{
    		return "CREDIT ****-****-" + cardLastFour;
    	}
    	else
    	{
    		return "CASH";
    	}
    }
    
    public long getAmountPaidCents() {
        return amountPaidCents;
    }

    public long getChangeDueCents() {
        return changeDueCents;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public String getMessage() {
        return message;
    }
}
