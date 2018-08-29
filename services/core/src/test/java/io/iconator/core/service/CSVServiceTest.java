package io.iconator.core.service;

import io.iconator.commons.model.db.KeyPairs;
import org.apache.commons.csv.CSVFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        CSVService.class
})
public class CSVServiceTest {

    @Autowired
    private CSVService csvService;

    @Test
    public void testFromCSV() throws Exception {
        InputStream inputStream = CSVServiceTest.class.getResourceAsStream("/publickeys-dev.csv");
        List<KeyPairs> keyPairsList = csvService.fromCSV(inputStream);
        assertEquals(100, keyPairsList.size());
    }

    @Test(expected = Exception.class)
    public void testFromCSV_Error_Reading_Format() throws Exception {
        Mockito.mock(CSVFormat.class);
        when(CSVFormat.DEFAULT.parse(any())).thenThrow(new Exception());

        InputStream inputStream = CSVServiceTest.class.getResourceAsStream("/publickeys-dev.csv");
        csvService.fromCSV(inputStream);
    }

}
