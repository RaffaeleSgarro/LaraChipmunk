package app;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ContactsServiceIntegrationTest {

    private ContactsService target;
    private List<Contact> results;
    private String query;

    @BeforeClass
    public void indexContacts() throws Exception {
        target = new ContactsService();
        InputStream csv = ContactsServiceIntegrationTest.class.getResourceAsStream("/app/contacts.csv");
        target.createIndexFromCsv(new InputStreamReader(csv, "UTF-8"));
        csv.close();
    }

    @BeforeMethod
    public void setUp() {
        results = null;
    }

    @Test
    public void gollumn() throws Exception {
        search("gol");

        shouldOnlyFind("Gollum");
    }

    @Test
    public void montezemolo() throws Exception {
        search("cor");

        shouldOnlyFind("Luca Cordero di Montezemolo");
    }

    @Test
    public void bimbominkia() throws Exception {
        search("bim");

        shouldOnlyFind("BìmbòMìnkìà @_@");
    }

    private void search(String query) throws Exception {
        this.query = query;
        this.results = target.search(query);
    }

    private void shouldOnlyFind(String name) {
        assertNotNull(results, "Results should not be null. Did you call search()?");
        assertEquals(results.size(), 1, "There should only be one search result for query " + query);
        assertEquals(results.get(0).getName(), name, "Query " + query + " returned the wrong record or there's a spelling error");
    }
}