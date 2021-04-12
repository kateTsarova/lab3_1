package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.fail;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

import java.lang.reflect.InvocationHandler;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

    @Mock
    private InvoiceFactory factory;
    @Mock
    private TaxPolicy taxPolicy;
    private BookKeeper keeper;

    @BeforeEach
    void setUp() throws Exception {

        keeper = new BookKeeper(factory);
    }

    @Test
    void test() {
        //fail("not implemented");
        Id sampleId = Id.generate();
        ClientData dummy = new ClientData(sampleId, "Kowalski");
        InvoiceRequest request = new InvoiceRequest(dummy);
        Invoice invoice = new Invoice(Id.generate(), dummy);
        when(factory.create(dummy)).thenReturn(invoice);


        Invoice issuance =  keeper.issuance(request, taxPolicy);
        assertTrue(nonNull(issuance));
        assertEquals(invoice, issuance);

    }

}