package app;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.*;

public class ContactsServiceExcelTest {

    private ContactsService target;
    private List<Contact> results;
    private String query;

    @BeforeClass
    public void indexContacts() throws Exception {
        target = new ContactsService();
        InputStream in = ContactsServiceExcelTest.class.getResourceAsStream("/app/contacts.xlsx");
        target.createIndexFromWorkbook(WorkbookFactory.create(in));
        in.close();
    }

    @BeforeMethod
    public void setUp() {
        results = null;
    }

    @Test
    public void mario() throws Exception {
        search("mario");

        shouldOnlyFind("MARIO ROSSI");
    }

    private void search(String query) throws Exception {
        this.query = query;
        this.results = target.search(query);
    }

    private void shouldOnlyFind(String name) {
        assertNotNull(results, "Results should not be null. Did you call search()?");
        assertFalse(results.isEmpty(), "Search for " + query + " didn't find anything! Expected one hit");
        assertEquals(results.size(), 1, "There should only be one search result for query " + query);
        assertEquals(results.get(0).getName(), name, "Query " + query + " returned the wrong record or there's a spelling error");
    }
}