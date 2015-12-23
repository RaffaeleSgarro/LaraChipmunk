package app;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsService {

    private Analyzer analyzer;
    private RAMDirectory indexDirectory;
    private IndexSearcher indexSearcher;
    private IndexWriter writer;

    public void resetIndex() throws Exception {
        analyzer = new StandardAnalyzer();
        indexDirectory = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(indexDirectory, conf);
    }

    public void commit() throws Exception {
        writer.commit();
        writer.close();
        indexSearcher = new IndexSearcher(DirectoryReader.open(indexDirectory));
    }

    public void createIndexFromWorkbook(Workbook workbook) throws Exception {
        resetIndex();
        Sheet sheet = workbook.getSheetAt(0);
        int current = 1; // skip header
        Row row;

        while ((row = sheet.getRow(current)) != null) {
            String first = row.getCell(0).getStringCellValue();
            String last = row.getCell(1).getStringCellValue();
            String email = row.getCell(2).getStringCellValue();

            addRecordToIndex(first + " " + last, email);

            current++;
        }

        commit();
    }

    public void createIndexFromCsv(Reader in) throws Exception {
        if (in == null)
            throw new RuntimeException("InputStream is null!");

        resetIndex();

        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader("name", "email")
                .withAllowMissingColumnNames(false)
                .withSkipHeaderRecord();

        CSVParser csv = new CSVParser(in, csvFormat);

        for (CSVRecord record : csv) {
            addRecordToIndex(record.get("name"), record.get("email"));
        }

        commit();
    }

    private void addRecordToIndex(String name, String email) throws Exception {
        Document doc = new Document();
        if (name != null && !name.isEmpty() && email != null && !email.isEmpty()) {
            doc.add(new TextField("name", name, Field.Store.YES));
            doc.add(new StringField("email", email, Field.Store.YES));
            writer.addDocument(doc);
        }
    }

    public List<Contact> search(String q) throws Exception {
        if (q.length() < 2)
            return Collections.emptyList();

        final String luceneQuery = q.endsWith("*") || q.endsWith(" ") ? q : q + "*";

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
