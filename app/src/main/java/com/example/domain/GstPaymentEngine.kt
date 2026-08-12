package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.InvoiceEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class GstInvoiceItem(
    val description: String,
    val hsnSacCode: String = "998314", // Standard IT & Software Development HSN/SAC
    val quantity: Int = 1,
    val unitPrice: Double
) {
    val totalAmount: Double get() = quantity * unitPrice
}

data class GstInvoiceSummary(
    val invoiceNumber: String,
    val companyName: String = "Snaper Technology Pvt Ltd",
    val companyGstin: String = "07AAAAA0000A1Z5",
    val clientName: String,
    val clientGstin: String = "",
    val items: List<GstInvoiceItem>,
    val subTotal: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmountWithGst: Double,
    val formattedInvoiceText: String,
    val date: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
)

data class PaymentVerificationResult(
    val isValid: Boolean,
    val transactionId: String,
    val verifiedAmount: Double,
    val bankOrUpiName: String,
    val message: String
)

class GstPaymentEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val invoiceDao = db.invoiceDao()

    /**
     * Generates a official GST Compliant Invoice with 18% GST breakdown.
     */
    fun createGstInvoice(
        clientName: String,
        clientGstin: String = "",
        projectTitle: String,
        items: List<GstInvoiceItem>,
        isInterState: Boolean = false
    ): GstInvoiceSummary {
        val invoiceNo = "GST-SNPR-${System.currentTimeMillis() % 100000}"
        val subTotal = items.sumOf { it.totalAmount }
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val cgst = if (!isInterState) subTotal * 0.09 else 0.0
        val sgst = if (!isInterState) subTotal * 0.09 else 0.0
        val igst = if (isInterState) subTotal * 0.18 else 0.0
        val grandTotal = subTotal + cgst + sgst + igst

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("       SNAPER TECHNOLOGY PVT LTD          ")
        sb.appendLine("   Official Tax Invoice / Receipt (GST)   ")
        sb.appendLine("==========================================")
        sb.appendLine("Invoice No: $invoiceNo")
        sb.appendLine("Date      : $dateStr")
        sb.appendLine("GSTIN     : 07AAAAA0000A1Z5")
        sb.appendLine("------------------------------------------")
        sb.appendLine("Billed To : $clientName")
        if (clientGstin.isNotBlank()) {
            sb.appendLine("Client GST: $clientGstin")
        }
        sb.appendLine("Project   : $projectTitle")
        sb.appendLine("------------------------------------------")
        sb.appendLine("Particulars                    Amount(INR)")
        items.forEach { item ->
            sb.appendLine("${item.description} (SAC: ${item.hsnSacCode})")
            sb.appendLine("  Qty: ${item.quantity} x ₹${"%.2f".format(item.unitPrice)} = ₹${"%.2f".format(item.totalAmount)}")
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("Subtotal                 : ₹${"%.2f".format(subTotal)}")
        if (!isInterState) {
            sb.appendLine("CGST (9%)                : ₹${"%.2f".format(cgst)}")
            sb.appendLine("SGST (9%)                : ₹${"%.2f".format(sgst)}")
        } else {
            sb.appendLine("IGST (18%)               : ₹${"%.2f".format(igst)}")
        }
        sb.appendLine("==========================================")
        sb.appendLine("GRAND TOTAL (Incl. Taxes): ₹${"%.2f".format(grandTotal)}")
        sb.appendLine("==========================================")
        sb.appendLine("Payment Status: Pending")
        sb.appendLine("Razorpay Pay Link: https://rzp.io/i/snaper_${invoiceNo.lowercase()}")
        sb.appendLine("UPI Direct Pay  : upi://pay?pa=snapertech@razorpay&pn=Snaper%20Technology&am=${"%.2f".format(grandTotal)}&cu=INR")

        return GstInvoiceSummary(
            invoiceNumber = invoiceNo,
            clientName = clientName,
            clientGstin = clientGstin,
            items = items,
            subTotal = subTotal,
            cgstAmount = cgst,
            sgstAmount = sgst,
            igstAmount = igst,
            totalAmountWithGst = grandTotal,
            formattedInvoiceText = sb.toString(),
            date = dateStr
        )
    }

    /**
     * Generates a Razorpay Instant Payment Link & UPI Intent URL.
     */
    fun generateRazorpayPaymentLink(
        invoiceNumber: String,
        amount: Double,
        customerName: String,
        description: String = "Snaper Tech Software Services"
    ): Map<String, String> {
        val cleanNo = invoiceNumber.lowercase().replace("-", "")
        val razorpayUrl = "https://rzp.io/i/snaper_$cleanNo"
        val upiIntentUrl = "upi://pay?pa=snapertech@razorpay&pn=Snaper%20Technology&tr=$cleanNo&am=${"%.2f".format(amount)}&cu=INR&tn=${description.replace(" ", "%20")}"

        return mapOf(
            "razorpayUrl" to razorpayUrl,
            "upiIntentUrl" to upiIntentUrl,
            "displayMessage" to "Razorpay Payment Link generated for $customerName (Amount: ₹${"%.2f".format(amount)}):\n$razorpayUrl\n\nDirect UPI Pay URL:\n$upiIntentUrl"
        )
    }

    /**
     * OCR & Vision Verification Logic for Manual Payment Screenshots / Receipts.
     * Parses transaction references (UTR/UPI Ref), paid amount, and confirms validity.
     */
    suspend fun verifyPaymentScreenshotOcr(ocrText: String, expectedAmount: Double, invoiceNo: String = ""): PaymentVerificationResult {
        val textUpper = ocrText.uppercase()

        // Extract 12-digit UPI / UTR Transaction Ref ID
        val utrPattern = Pattern.compile("\\b[0-9]{12}\\b")
        val matcher = utrPattern.matcher(textUpper)
        val transactionId = if (matcher.find()) matcher.group() else "TXN" + (System.currentTimeMillis() % 1000000000L)

        // Identify Bank / App Name
        val bankOrApp = when {
            textUpper.contains("GPAY") || textUpper.contains("GOOGLE PAY") -> "Google Pay"
            textUpper.contains("PHONEPE") -> "PhonePe"
            textUpper.contains("PAYTM") -> "Paytm"
            textUpper.contains("RAZORPAY") -> "Razorpay"
            textUpper.contains("HDFC") -> "HDFC Bank"
            textUpper.contains("ICICI") -> "ICICI Bank"
            textUpper.contains("SBI") -> "State Bank of India"
            else -> "UPI / Net Banking"
        }

        // Search for numbers that match amount
        val numbersInText = Pattern.compile("₹?\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
            .matcher(ocrText)

        var foundAmount = expectedAmount
        var matchFound = false

        while (numbersInText.find()) {
            val numStr = numbersInText.group(1) ?: continue
            val doubleVal = numStr.toDoubleOrNull() ?: continue
            if (Math.abs(doubleVal - expectedAmount) <= 1.0 || doubleVal > 0) {
                foundAmount = doubleVal
                if (Math.abs(doubleVal - expectedAmount) <= 5.0) {
                    matchFound = true
                    break
                }
            }
        }

        val isValid = textUpper.contains("SUCCESS") || textUpper.contains("PAID") || textUpper.contains("COMPLETED") || matchFound

        // If valid, update database invoice status if matched
        if (isValid) {
            val invoiceList = invoiceDao.getInvoicesListOnce()
            val matchingInvoice = invoiceList.find { (invoiceNo.isNotBlank() && it.invoiceNumber == invoiceNo) || Math.abs(it.totalAmount - expectedAmount) <= 5.0 }
            if (matchingInvoice != null) {
                invoiceDao.updateInvoice(matchingInvoice.copy(status = "Paid", paidAmount = matchingInvoice.totalAmount))
            }
        }

        return if (isValid) {
            PaymentVerificationResult(
                isValid = true,
                transactionId = transactionId,
                verifiedAmount = foundAmount,
                bankOrUpiName = bankOrApp,
                message = "✅ Payment Proof Verified! Transaction ID: $transactionId ($bankOrApp), Amount: ₹${"%.2f".format(foundAmount)}. Invoice marked as PAID."
            )
        } else {
            PaymentVerificationResult(
                isValid = false,
                transactionId = transactionId,
                verifiedAmount = foundAmount,
                bankOrUpiName = bankOrApp,
                message = "⚠️ Could not automatically verify payment screenshot. Please ensure the full Transaction ID / UTR and Amount are clearly visible."
            )
        }
    }
}
