package org.jcoderz.testdata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import nu.xom.xinclude.XIncluder;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Table;

/**
 * Generic Test data importer. Recursively scans a directory for test data items and
 * submits them via JDBC to a predefined database. The tables and attribute names are
 * mapped straightforward to the hibernate configuration expected on the classpath. This
 * implementation relies on auto index of all tables.
 *
 * @author Torsten Stolpmann
 */
public class Importer {

    private static final int ERROR_EXIT_CODE = 20;

    private static final String DEFAULT_TABLE_PREFIX = "S0IR_";

    private static final long DEFAULT_SEQUENCE_BASE = 100000L;

    private static final String SUFFIX_XML = ".xml";

    private static final String FILE_SEPARATOR = "/";

    private static final XPathContext TD_CONTEXT = new XPathContext("td",
            "http://jcoderz.org/test-data");

    static final Logger logger = Logger.getLogger(Importer.class.getName());

    private String tablePrefix;
    private long sequenceBase;

    private Connection connection;

    /**
     * The main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: " + Importer.class.getName()
                    + " <sourceDirectory>");
        }

        String sourceDirectory = args[0];

        Properties properties = new Properties();
        List<String> tableNames = new ArrayList<String>();

        logger.info("Starting import ...");

        Locale.setDefault(Locale.GERMANY);

        try {
            String defaultPropertiesName = "/testdata.properties";

            InputStream defaultInput = Importer.class
                    .getResourceAsStream(defaultPropertiesName);
            if (defaultInput != null) {
                logger.fine("Loading default properties (" + defaultPropertiesName + ")");
                properties.load(defaultInput); //$NON-NLS-1$
            } else {
                logger.fine("Default properties not found");
            }
            String userPropertiesName = FILE_SEPARATOR + System.getProperty("user.name")
                    + ".properties";
            InputStream userInput = Importer.class
                    .getResourceAsStream(userPropertiesName);
            if (userInput != null) {
                logger.fine("Loading user properties");
                properties.load(userInput); //$NON-NLS-1$
            } else {
                logger.fine("User properties (" + userPropertiesName + ") not found");
            }
            System.getProperties().putAll(properties);
            logger.warning(System.getProperties().toString());
            properties = System.getProperties();

            Configuration hibernateConfig = new Configuration();
            hibernateConfig.addProperties(properties);
            hibernateConfig.configure();
            hibernateConfig.getProperties().remove("hibernate.connection.datasource");
            hibernateConfig.getProperties().remove(
                    "hibernate.transaction.manager_lookup_class");
            hibernateConfig.getProperties().remove("hibernate.transaction.factory_class");

            Iterator<?> iter = hibernateConfig.getTableMappings();
            while (iter.hasNext()) {
                Table table = (Table) iter.next();
                tableNames.add(table.getName().toUpperCase());
            }
            SessionFactory factory = hibernateConfig.buildSessionFactory();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception: ", e);
            System.exit(ERROR_EXIT_CODE);
        }

        Importer importer = new Importer(DEFAULT_TABLE_PREFIX, DEFAULT_SEQUENCE_BASE);

        Map<String, Document> items = new HashMap<String, Document>();

        if (!importer.loadItems(sourceDirectory, items)) {
            logger.log(Level.SEVERE, "Invalid test data set detected - aborting.");
            System.exit(ERROR_EXIT_CODE);
        }

        Map<String, Set<String>> dependencyMap = importer.buildDependencyMap(items);

        if (importer.validateDependencies(items, dependencyMap)) {
            List<String> result = new ArrayList<String>();
            List<String> leftover = new ArrayList<String>();

            importer.reOrderItems(items, dependencyMap, result, leftover);

            List<String> queries = new ArrayList<String>();
            if (importer.generateQueries(result, items, queries, null, tableNames, properties,
                    false)) {
                logger.info("Execution plan for constrained inserts:");
                logger.info("---------------------------------------");
                for (String query : queries) {
                    logger.info(query);
                }
                logger.info("---------------------------------------");

            } else {
                logger
                        .severe("No insert statements have been executed since errors have been detected.");
                System.exit(ERROR_EXIT_CODE);
            }

            List<String> leftOverQueries = new ArrayList<String>();
            Set<String> postQueries = new HashSet<String>();

            if (importer.generateQueries(leftover, items, leftOverQueries, postQueries, tableNames,
                    properties, true)) {
                logger.info("Execution plan for unconstrained inserts:");
                logger.info("-----------------------------------------");
                for (String query : leftOverQueries) {
                    logger.info(query);
                }

                for (String query : postQueries) {
                    logger.info(query);
                }
                logger.info("---------------------------------------");

            } else {
                logger
                        .severe("No insert statements have been executed since errors have been detected.");
                System.exit(ERROR_EXIT_CODE);
            }

            importer.executeQueries(properties, queries, false);
            importer.executeQueries(properties, leftOverQueries, false);
            importer.executeQueries(properties, postQueries, true);

            try {
                Connection conn = importer.getConnection(properties);
                conn.close();
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Exception: ", e);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Exception: ", e);
            }
        } else {
            logger
                    .severe("No insert statements have been executed since errors have been detected.");
            System.exit(ERROR_EXIT_CODE);
        }
    }

    public Importer(final String prefix, final long base) {

        tablePrefix = prefix;
        sequenceBase = base;

    }

    /**
     * Execute the supplied SQL queries.
     *
     * @param hibernateProperties the hibernate properties.
     * @param queries the queries
     */
    private void executeQueries(final Properties hibernateProperties,
            final Collection<String> queries, boolean delayErrors) {

        boolean success = true;
        try {
            Connection conn = getConnection(hibernateProperties);

            for (SQLWarning warn = conn.getWarnings(); warn != null; warn = warn
                    .getNextWarning()) {
                logger.warning("SQL Warning:");
                logger.warning("State  : " + warn.getSQLState());
                logger.warning("Message: " + warn.getMessage());
                logger.warning("Error  : " + warn.getErrorCode());
            }

            Statement langStmt = conn.createStatement();
            String alterStatement = "ALTER SESSION SET NLS_TERRITORY='AMERICA'";
            langStmt.execute(alterStatement);
            langStmt.close();

            for (String query : queries) {

                try {
                    logger.info(query);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    rs.close();
                    stmt.close();
                    conn.commit();
                } catch (SQLException e) {

                    logger.severe("SQL Exception:");

                    while (e != null) {
                        logger.severe("State  : " + e.getSQLState());
                        logger.severe("Message: " + e.getMessage());
                        logger.severe("Error  : " + e.getErrorCode());

                        e = e.getNextException();
                    }
                    if (!delayErrors) {
                        System.exit(ERROR_EXIT_CODE);
                    }
                    success = false;
                }
            }
        } catch (SQLException e) {

            logger.severe("SQL Exception:");

            while (e != null) {
                logger.severe("State  : " + e.getSQLState());
                logger.severe("Message: " + e.getMessage());
                logger.severe("Error  : " + e.getErrorCode());

                e = e.getNextException();
            }
            if (!delayErrors) {
                System.exit(ERROR_EXIT_CODE);
            }
            success = false;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e);
            System.exit(ERROR_EXIT_CODE);
        }
        if (delayErrors && !success) {
            System.exit(ERROR_EXIT_CODE);
        }
    }

    /**
     * Write the map of documents.
     *
     * @param items the map of items
     * @param queries the resulting queries
     * @param postQueries the post insert queries
     * @param idList the ordered list of item names
     * @param tableNames the table names
     * @param hibernateProperties the hibernate properties.
     * @param disableConstraints true if constraints should be disabled here.
     * @return true, if write
     */
    private boolean generateQueries(final List<String> idList,
            final Map<String, Document> items, final List<String> queries,
            final Set<String> postQueries,
            final List<String> tableNames, final Properties hibernateProperties,
            final boolean disableConstraints) {

        boolean valid = true;

        final Set<String> preQueries = new HashSet<String>();
        final List<String> insertQueries = new ArrayList<String>();

        for (String id : idList) {

            StringBuffer query = new StringBuffer();
            StringBuffer names = new StringBuffer("(");
            StringBuffer values = new StringBuffer("(");

            Document item = items.get(id);
            String tableName = getTableName(item);

            if (!tableNames.contains(tableName)) {
                logger.severe("The type " + getItemType(item) + " of item "
                        + getItemId(item)
                        + " has no matching table in the current configuration ("
                        + tableName + ")");
                valid = false;
            }
            Map<String, Node> attributes = getAttributes(item);

            int position = 0;
            int end = attributes.entrySet().size();

            for (Map.Entry<String, Node> attribute : attributes.entrySet()) {

                position++;

                String name = attribute.getKey().toUpperCase();
                Node node = attribute.getValue();

                if (node instanceof Element) {
                    Element element = (Element) node;
                    if (element.getLocalName().equals("value")) {
                        names.append(name);
                        values.append("'");
                        values.append(element.getValue());
                        values.append("'");
                    } else if (element.getLocalName().equals("autovalue")) {
                        names.append(name);
                        values.append(getAsPrimaryKey(id));
                    } else if (element.getLocalName().equals("ref")) {
                        String reference = element.getValue();
                        if (reference.length() > 0) {
                            if (items.containsKey(reference)) {
                                names.append(name);
                                values.append(getAsPrimaryKey(element.getValue()));
                            } else {
                                logger.severe("ERROR! the reference " + reference
                                        + " could not be resolved.");
                                valid = false;
                            }
                        }
                    }
                }
                if (position == end) {
                    names.append(") ");
                    values.append(") ");
                } else {
                    names.append(", ");
                    values.append(", ");
                }
            }
            if (attributes.size() > 0) {
                query.append("INSERT INTO " + tableName);
                query.append(" ");
                query.append(names);
                query.append(" VALUES ");
                query.append(values);
            }
            logger.fine(query.toString());
            if (disableConstraints) {
                preQueries.addAll(buildEnableConstraints(false, tableName,
                        hibernateProperties));
                insertQueries.add(query.toString());
                postQueries.addAll(buildEnableConstraints(true, tableName,
                        hibernateProperties));
            } else {
                insertQueries.add(query.toString());
            }
        }
        queries.addAll(preQueries);
        queries.addAll(insertQueries);
        return valid;
    }

    /**
     * Reorder the items.
     *
     * @param items the document items.
     * @param dependencyMap the dependency map
     * @param result the resulting list of id's
     * @param leftover the leftover items if any.
     */
    private void reOrderItems(final Map<String, Document> items,
            final Map<String, Set<String>> dependencyMap, final List<String> result,
            final List<String> leftover) {

        boolean changed;
        logger.fine("Reordering " + items.size() + " items ...");
        final Map<String, Document> itemPool = new HashMap<String, Document>(items);
        do {
            changed = false;

            Iterator<Map.Entry<String, Document>> entries = itemPool.entrySet()
                    .iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Document> item = entries.next();
                String id = item.getKey();
                boolean accepted = true;

                Set<String> dependencies = dependencyMap.get(id);
                for (String dependency : dependencies) {
                    if (!result.contains(dependency)) {
                        accepted = false;
                        break;
                    }
                }
                if (accepted) {
                    List<String> declaredDependencies = getDeclaredDependencies(item
                            .getValue());
                    for (String declaredDependency : declaredDependencies) {
                        if (!result.contains(declaredDependency)) {
                            accepted = false;
                            break;
                        }
                    }
                }

                if (accepted) {
                    logger.fine("Accepting item: " + id);
                    entries.remove();
                    result.add(id);
                    changed = true;
                }
            }
        } while (changed);
        logger.fine("Accepted " + result.size() + " items ...");

        if (!itemPool.isEmpty()) {
            logger
                    .warning("Reference loop(s) detected! The following items will be added ignoring db-constraints:");
            for (String id : itemPool.keySet()) {
                leftover.add(id);
                StringBuffer dependencyList = new StringBuffer();
                Set<String> dependencies = dependencyMap.get(id);
                for (String dependency : dependencies) {
                    dependencyList.append(dependency);
                    dependencyList.append(" ");
                }

                logger.warning(id + ": [" + dependencyList.toString() + "]");
            }
        }
    }

    /**
     * Validate dependencies.
     *
     * @param items the items
     * @param dependencyMap the dependency map
     * @return true, if successful
     */
    private boolean validateDependencies(Map<String, Document> items,
            Map<String, Set<String>> dependencyMap) {

        boolean valid = true;

        for (Map.Entry<String, Set<String>> dependenciesEntry : dependencyMap.entrySet()) {

            String id = dependenciesEntry.getKey();
            Set<String> dependencies = dependenciesEntry.getValue();
            for (String dependency : dependencies) {
                if (!items.containsKey(dependency)) {
                    logger.severe("Item " + id + " contains invalid reference: "
                            + dependency + " (Note: Self references are not allowed!)");
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Builds the dependency map.
     *
     * @param items the items to build from.
     * @return the Map of dependencies.
     */
    private Map<String, Set<String>> buildDependencyMap(final Map<String, Document> items) {

        Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();

        for (Map.Entry<String, Document> item : items.entrySet()) {

            String id = item.getKey();
            Document document = item.getValue();

            Set<String> dependencies = new HashSet<String>();

            Nodes references = document.query("/td:item/td:attribute/td:ref", TD_CONTEXT);

            for (int j = 0; j < references.size(); j++) {
                Node node = references.get(j);
                String refId = node.getValue();
                if (refId.length() > 0) {
                    dependencies.add(refId);
                } else {
                    // Drop empty references.
                    node.getParent().detach();
                }
            }

            dependencyMap.put(id, dependencies);
        }
        return dependencyMap;
    }

    /**
     * Recursively scan the directory and add found items to the map.
     *
     * @param dirName the directory name
     * @param itemMap the item map
     */
    private boolean loadItems(String dirName, Map<String, Document> itemMap) {

        File rootDir = new File(dirName);
        if (!rootDir.exists()) {
            logger.severe("Root directory '" + dirName + "' not found.");
            return false;
        }
        String[] entries = rootDir.list();
        boolean success = true;

        for (int i = 0; i < entries.length; i++) {
            String entry = dirName + FILE_SEPARATOR + entries[i];
            File subFile = new File(entry);
            if (subFile.isDirectory()) {
                success &= loadItems(entry, itemMap);
            } else {
                if (subFile.getName().endsWith(SUFFIX_XML)) {
                    Builder parser = new Builder(false);
                    try {
                        Document raw = parser.build(entry);
                        Document document = XIncluder.resolve(raw);
                        String id = getItemId(document);

                        if (itemMap.containsKey(id)) {
                            logger.severe("Duplicate id detected: " + id
                                    + " - item will be skipped");
                            success = false;
                        } else {
                            itemMap.put(id, document);
                            logger.fine("Adding item: " + id);
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error parsing file " + entry + ": ", e);
                    }
                }
            }
        }
        return success;
    }

    /**
     * Gets the supplied id as a primary key representation.
     *
     * @param id the id
     * @return the primary key
     */
    protected String getAsPrimaryKey(final String id) {

        return "" + (sequenceBase + Long.parseLong(id.substring(3)));
    }

    /**
     * Returns the table name for the given item.
     *
     * @param item the item
     * @return the table name
     */
    protected String getTableName(final Document item) {

        Node type = item.query("//td:item/td:type", TD_CONTEXT).get(0);
        return tablePrefix + type.getValue().toUpperCase();
    }

    /**
     * Gets the item id.
     *
     * @param document the document containing the item.
     * @return the item id if any, null else.
     */
    private String getItemType(final Document document) {

        String type = null;

        Nodes nodes = document.query("//td:item/td:type", TD_CONTEXT);
        for (int j = 0; j < nodes.size(); j++) {
            Node node = nodes.get(j);
            type = node.getValue();
            break;
        }
        if (type == null) {
            logger.severe(getItemId(document) + " contains no type element!");
        }
        return type;
    }

    /**
     * Gets the item id.
     *
     * @param document the document containing the item.
     * @return the item id if any, null else.
     */
    private String getItemId(final Document document) {

        String id = null;

        Nodes nodes = document.query("//td:item/td:id", TD_CONTEXT);
        for (int j = 0; j < nodes.size(); j++) {
            Node node = nodes.get(j);
            id = node.getValue();
            break;
        }
        return id;
    }

    /**
     * Return the item attributes.
     *
     * @param document the document containing the item.
     * @return the attributes of this item.
     */
    private Map<String, Node> getAttributes(final Document document) {

        Map<String, Node> attributeMap = new HashMap<String, Node>();

        Nodes attributes = document.query("//td:item/td:attribute", TD_CONTEXT);
        for (int j = 0; j < attributes.size(); j++) {
            Node attribute = attributes.get(j);
            String name = attribute.query("td:name", TD_CONTEXT).get(0).getValue();
            Nodes values = attribute.query("td:value|td:autovalue|td:ref", TD_CONTEXT);
            if (values.size() > 0) {
                attributeMap.put(name, values.get(0));
            }
        }
        return attributeMap;
    }

    /**
     * Return the explicit item dependencies.
     *
     * @param document the document containing the item.
     * @return the explicit dependencies of this item.
     */
    private List<String> getDeclaredDependencies(final Document document) {

        List<String> result = new ArrayList<String>();

        Nodes dependencies = document.query("//td:item/td:dependency", TD_CONTEXT);
        for (int j = 0; j < dependencies.size(); j++) {
            Node dependency = dependencies.get(j);
            result.add(dependency.getValue());
        }
        return result;
    }

    /**
     * Enable database constraints.
     *
     * @param enabled the enabled flag
     * @param tableName the table name
     * @param hibernateProperties the hibernate properties
     * @return the list of queries
     */
    private List<String> buildEnableConstraints(final boolean enabled,
            final String tableName, final Properties hibernateProperties) {

        final List<String> queries = new ArrayList<String>();
        List<String> constraints = getConstraints(tableName, hibernateProperties);
        for (String constraint : constraints) {

            final String query;
            if (enabled) {
                query = "ALTER TABLE " + tableName + " ENABLE CONSTRAINT " + constraint;
            } else {
                query = "ALTER TABLE " + tableName + " DISABLE CONSTRAINT " + constraint;
            }
            queries.add(query);
        }
        return queries;
    }

    private List<String> getConstraints(String tableName, Properties hibernateProperties) {

        final List<String> constraints = new ArrayList<String>();
        try {
            Connection conn = getConnection(hibernateProperties);

            for (SQLWarning warn = conn.getWarnings(); warn != null; warn = warn
                    .getNextWarning()) {
                logger.warning("SQL Warning:");
                logger.warning("State  : " + warn.getSQLState());
                logger.warning("Message: " + warn.getMessage());
                logger.warning("Error  : " + warn.getErrorCode());
            }

            String query = "select constraint_name from user_constraints where table_name ='"
                    + tableName + "' and constraint_type='R'";

            logger.info(query);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String constraint = rs.getString(1);
                constraints.add(constraint);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {

            logger.severe("SQL Exception:");

            while (e != null) {
                logger.severe("State  : " + e.getSQLState());
                logger.severe("Message: " + e.getMessage());
                logger.severe("Error  : " + e.getErrorCode());

                e = e.getNextException();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception: ", e);
        }
        return constraints;
    }

    /**
     * Gets the database connection for the given properties.
     *
     * @param hibernateProperties the hibernate properties
     * @return the connection
     * @throws ClassNotFoundException the class not found exception
     * @throws SQLException the SQL exception
     */
    private Connection getConnection(final Properties hibernateProperties)
            throws ClassNotFoundException, SQLException {

        if (connection == null) {
            Class.forName(hibernateProperties
                    .getProperty("hibernate.connection.driver_class"));

            Properties info = new Properties();
            info.setProperty("user", hibernateProperties
                    .getProperty("hibernate.connection.username"));
            info.setProperty("password", hibernateProperties
                    .getProperty("hibernate.connection.password"));
            info.setProperty("useUnicode", "true");
            info.setProperty("characterEncoding", "UTF-8");

            connection = DriverManager.getConnection(hibernateProperties
                    .getProperty("hibernate.connection.url"), info);
        }
        return connection;
    }
}
