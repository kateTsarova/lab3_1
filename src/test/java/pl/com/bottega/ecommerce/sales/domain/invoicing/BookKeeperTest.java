package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.fail;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductDataBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import static org.mockito.ArgumentMatchers.any;

import java.lang.reflect.InvocationHandler;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

    private BookKeeper keeper;

    private static final Id SAMPLE_CLIENT_ID = Id.generate();
    private static final String SAMPLE_CLIENT_NAME = "Kowalski";
    private static final ClientData SAMPLE_CLIENT_DATA = new ClientData(SAMPLE_CLIENT_ID, SAMPLE_CLIENT_NAME);
    private static final Tax SAMPLE_TAX = new Tax(Money.ZERO, "tax name");
    private static final Id SAMPLE_INVOICE_ID = Id.generate();
    private static final String SAMPLE_PRODUCT_DATA_NAME = "Product name";

    @Mock
    private InvoiceFactory factory;
    @Mock
    private TaxPolicy taxPolicy;


    @BeforeEach
    void setUp() throws Exception {
        keeper = new BookKeeper(factory);
    }

    @Test
    public void conditionTest_InvoiceWithOneItem_RequestContainsOneItem() {
        InvoiceRequest oneItem = new InvoiceRequest(SAMPLE_CLIENT_DATA);

        ProductData productDummy = new ProductDataBuilder().productId(Id.generate())
                .price(Money.ZERO)
                .name(SAMPLE_PRODUCT_DATA_NAME)
                .productType(ProductType.STANDARD)
                .snapshotDate(null)
                .build();

        RequestItem requestDummy = new RequestItem(productDummy, 1, Money.ZERO);
        oneItem.add(requestDummy);

        Mockito.when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(SAMPLE_TAX);

        Invoice sampleInvoice = new Invoice(SAMPLE_INVOICE_ID, SAMPLE_CLIENT_DATA);
        Mockito.when(factory.create(SAMPLE_CLIENT_DATA)).thenReturn(sampleInvoice);

        int expected = 1;
        Invoice invoice = keeper.issuance(oneItem, taxPolicy);
        assertEquals(expected, invoice.getItems().size());
    }

}