package app;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsService {

    private Analyzer analyzer;
    private RAMDirectory indexDirectory;
    private IndexSearcher indexSearcher;

    public void createIndexFromCsv(Reader in) throws Exception {
        if (in == null)
            throw new RuntimeException("InputStream is null!");

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader("name", "email")
                .withAllowMissingColumnNames(false)
                .withSkipHeaderRecord();

        CSVParser csv = new CSVParser(in, csvFormat);

        analyzer = new ItalianAnalyzer();
        indexDirectory = new RAMDirectory();

        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDirectory, conf);

        for (CSVRecord record : csv) {
            Document doc = new Document();
            String name = record.get("name");
            String email = record.get("email");
            if (name != null && !name.isEmpty() && email != null && !email.isEmpty()) {
                doc.add(new TextField("name", name, Field.Store.YES));
                doc.add(new StringField("email", email, Field.Store.YES));
                writer.addDocument(doc);
            }
        }

        writer.commit();
        writer.close();

        indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
    }

    public List<Contact> search(String q) throws Exception {
        if (q.length() < 2)
            return Collections.emptyList();

        final String luceneQuery = q.endsWith("*") ? q : q + "*";

        QueryParser parser = new QueryParser("name", analyzer);
        Query query = parser.parse(luceneQuery);

        TopDocs result = indexSearcher.search(query, 11);

        List<Contact> contacts = new ArrayList<>();

        for (ScoreDoc scoreDoc : result.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            Contact contact = new Contact();
            contacts.add(contact);
            contact.setName(document.get("name"));
            contact.setEmail(document.get("email"));
        }

        return contacts;
    }

}
