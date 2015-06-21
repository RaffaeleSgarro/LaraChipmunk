package app;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.testng.Assert.*;

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
    public void gollumn_beginning() throws Exception {
        search("gol");

        shouldOnlyFind("Gollum");
    }

    @Test
    public void montezemolo_beginning() throws Exception {
        search("cor");

        shouldOnlyFind("Luca Cordero di Montezemolo");
    }

    @Test
    public void montezemolo_fullTerm() throws Exception {
        search("montezemolo");

        shouldOnlyFind("Luca Cordero di Montezemolo");
    }

    @Test
    public void bimbominkia_beginning() throws Exception {
        search("bim");

        shouldOnlyFind("BìmbòMìnkìà @_@");
    }

    @Test
    public void alasia_fullTerm() throws Exception {
        search("alasia");

        shouldOnlyFind("Giuseppe Alasia");
    }

    @Test
    public void alasia_withoutLastLetter() throws Exception {
        search("alasi");

        shouldOnlyFind("Giuseppe Alasia");
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