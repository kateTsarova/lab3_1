package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductDataBuilder;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

    private BookKeeper keeper;

    private static final Id SAMPLE_CLIENT_ID = Id.generate();
    private static final String SAMPLE_CLIENT_NAME = "Kowalski";
    private static final ClientData SAMPLE_CLIENT_DATA = new ClientData(SAMPLE_CLIENT_ID, SAMPLE_CLIENT_NAME);
    private static final Tax SAMPLE_TAX = new Tax(Money.ZERO, "test tax name");
    private static final Id SAMPLE_INVOICE_ID = Id.generate();
    private static final String SAMPLE_PRODUCT_DATA_NAME = "test product name";

    @Mock
    private InvoiceFactory factory;
    @Mock
    private TaxPolicy taxPolicy;


    @BeforeEach
    void setUp() throws Exception {
        keeper = new BookKeeper(factory);
    }

    @Test
    public void conditionTest_invoiceWithOneItem_requestContainsOneItem() {
        InvoiceRequest request = new InvoiceRequest(SAMPLE_CLIENT_DATA);

        ProductData productDummy = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();

        RequestItem requestDummy = new RequestItem(productDummy, 1, Money.ZERO);
        request.add(requestDummy);

        Mockito.when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(SAMPLE_TAX);

        Invoice sampleInvoice = new Invoice(SAMPLE_INVOICE_ID, SAMPLE_CLIENT_DATA);
        Mockito.when(factory.create(SAMPLE_CLIENT_DATA)).thenReturn(sampleInvoice);

        int expected = 1;
        Invoice invoice = keeper.issuance(request, taxPolicy);
        assertEquals(expected, invoice.getItems().size());
    }

    @Test
    void conditionTest_emptyInvoice_requestContainsZeroItems() {
        InvoiceRequest request = new InvoiceRequest(SAMPLE_CLIENT_DATA);
        Invoice invoice = new Invoice(Id.generate(), SAMPLE_CLIENT_DATA);
        when(factory.create(SAMPLE_CLIENT_DATA)).thenReturn(invoice);

        Invoice actualInvoice = keeper.issuance(request, taxPolicy);

        int expected = 0;

        assertEquals(expected, actualInvoice.getItems().size());
    }

    @Test
    public void conditionTest_invoiceWithMultipleItem_requestContainsMultipleItems() {
        InvoiceRequest request = new InvoiceRequest(SAMPLE_CLIENT_DATA);

        ProductData productDummy = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();
        RequestItem requestDummy = new RequestItem(productDummy, 1, Money.ZERO);

        ProductData productDummy2 = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();
        RequestItem requestDummy2 = new RequestItem(productDummy2, 1, Money.ZERO);

        request.add(requestDummy);
        request.add(requestDummy2);

        Mockito.when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(SAMPLE_TAX);

        Invoice sampleInvoice = new Invoice(SAMPLE_INVOICE_ID, SAMPLE_CLIENT_DATA);
        Mockito.when(factory.create(SAMPLE_CLIENT_DATA)).thenReturn(sampleInvoice);

        int expected = 2;
        Invoice invoice = keeper.issuance(request, taxPolicy);
        assertEquals(expected, invoice.getItems().size());
    }

    public void behaviorTest_invokeCalculateTax_RequestContainsTwoItemsWithCorrespondingParameters() {
        InvoiceRequest request = new InvoiceRequest(SAMPLE_CLIENT_DATA);
        Invoice sampleInvoice = new Invoice(SAMPLE_INVOICE_ID, SAMPLE_CLIENT_DATA);

        ProductData product1 = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();

        Money totalCost1 = new Money(10, Money.DEFAULT_CURRENCY);
        RequestItem requestItem1 = new RequestItem(product1, 1, totalCost1);
        request.add(requestItem1);

        ProductData product2 = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();

        Money totalCost2 = new Money(2, Money.DEFAULT_CURRENCY);
        RequestItem requestItem2 = new RequestItem(product2, 2, totalCost2);
        request.add(requestItem2);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(SAMPLE_TAX);
        when(factory.create(SAMPLE_CLIENT_DATA)).thenReturn(sampleInvoice);

        ArgumentCaptor<ProductType> productTypeCaptor = ArgumentCaptor.forClass(ProductType.class);
        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);

        int expected = 2;

        keeper.issuance(request, taxPolicy);

        verify(taxPolicy, times(expected)).calculateTax(productTypeCaptor.capture(), moneyCaptor.capture());

        List<ProductType> capturedProductTypes = productTypeCaptor.getAllValues();
        List<Money> capturedMoney = moneyCaptor.getAllValues();

        assertEquals(product1.getType(), capturedProductTypes.get(0));
        assertEquals(totalCost1, capturedMoney.get(0));

        assertEquals(product2.getType(), capturedProductTypes.get(1));
        assertEquals(totalCost2, capturedMoney.get(1));
    }

}