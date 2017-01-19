/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

//@RunWith(AndroidJUnit4.class)
//@SmallTest
public class XmlModelTest {

    public static final String NAMESPACE = "";

    public static final double DOUBLE_VALUE = 3.14159243684;

    public static final double DELTA = 1e-9;

    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<LevelOne levelone=\"false\">\n" +
        "\t<LevelTwoA leveltwo=\"2\">\n" +
        "\t\t<LevelThreeA levelthree=\"" + DOUBLE_VALUE + "\"/>\n" +
        "\t</LevelTwoA>\n" +
        "\t<LevelTwoB leveltwo=\"" + DOUBLE_VALUE + "\">\n" +
        "\t\t<LevelThreeB levelthree=\"testthreeb\">\n" +
        "\t\t\tLevel Three B\n" +
        "\t\t</LevelThreeB>\n" +
        "\t</LevelTwoB>\n" +
        "\t<LevelTwoC>\n" +
        "\t\t<LevelThreeC>\n" +
        "\t\t\t<Value>A_AAA</Value>\n" +
        "\t\t\t<Value>B_AAA</Value>\n" +
        "\t\t\t<Value>C_AAA</Value>\n" +
        "\t\t\t<LevelFourD>\n" +
        "\t\t\t\t<Value>A_BBB</Value>\n" +
        "\t\t\t\t<Value>B_BBB</Value>\n" +
        "\t\t\t\t<LevelFive levelfive=\"true\"/>\n" +
        "\t\t\t</LevelFourD>\n" +
        "\t\t</LevelThreeC>\n" +
        "\t</LevelTwoC>\n" +
        "</LevelOne>";

    protected XmlModelParser context;

    protected XmlModel root;

//    /**
//     * This class mimics the functionality required when mutiple elements with an identical field are used in a
//     * document.
//     */
//    protected static class MultipleEntryElement extends XmlModel {
//
//        protected Set<String> values = new HashSet<>();
//
//        public MultipleEntryElement(String namespaceUri) {
//            super(namespaceUri);
//        }
//
//        @Override
//        public void parseField(QName keyName, Object value) {
//
//            if (keyName.equals(NAME)) {
//                Set<String> values = (Set<String>) this.getField(NAME);
//                if (values == null) {
//                    values = new HashSet<>();
//                    super.parseField(NAME, values);
//                }
//                values.add(((XmlModel) value).getCharactersContent().toString());
//            }
//        }
//    }

//    @Before
//    public void setup() throws Exception {
//
//        this.context = new XmlModelParser(NAMESPACE);
//        this.context.registerParsableModel(new QName(NAMESPACE, "LevelThreeC"), new MultipleEntryElement(NAMESPACE));
//        this.context.registerParsableModel(new QName(NAMESPACE, "LevelFourD"), new MultipleEntryElement(NAMESPACE));
//        InputStream is = new ByteArrayInputStream(XML.getBytes());
//        this.context.setParserInput(is);
//
//        this.root = new XmlModel(NAMESPACE);
//
//        this.root.read(this.context);
//    }
//
//    @Test
//    public void testGetInheritedField() {
//
//        // Get the leaf node for which to query for inherited values
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoC"));
//        // Expected Values
//        Set<String> levelThreeExpectedValues = new HashSet<>();
//        levelThreeExpectedValues.addAll(Arrays.asList("A_AAA", "B_AAA", "C_AAA"));
//        Set<String> levelFourAndFiveExpectedValues = new HashSet<>();
//        levelFourAndFiveExpectedValues.addAll(Arrays.asList("A_BBB", "B_BBB"));
//
//        // LevelThreeC includes its own values for the Value element
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeC"));
//        Set<String> levelThreeInheritedValues = (Set<String>) model.getInheritedField(MultipleEntryElement.NAME);
//
//        // LevelFourD contains Value elements and should not have inherited ones
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFourD"));
//        Set<String> levelFourInheritedValues = (Set<String>) model.getInheritedField(MultipleEntryElement.NAME);
//
//        // LevelFive contains no Value elements but should inherited the Values in only the preceding level
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFive"));
//        Set<String> levelFiveInheritedValues = (Set<String>) model.getInheritedField(MultipleEntryElement.NAME);
//
//        assertEquals("Only present value no inheritance", levelThreeExpectedValues, levelThreeInheritedValues);
//        assertEquals("Inherited with present values", levelFourAndFiveExpectedValues, levelFourInheritedValues);
//        assertEquals("Inherited with no own values", levelFourAndFiveExpectedValues, levelFiveInheritedValues);
//    }
//
//    @Test
//    public void testGetAdditiveInheritedField() {
//
//        // Get the leaf node for which to query for inherited values
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoC"));
//        // Expected Values
//        Set<String> levelThreeExpectedValues = new HashSet<>();
//        levelThreeExpectedValues.addAll(Arrays.asList("A_AAA", "B_AAA", "C_AAA"));
//        Set<String> levelFourAndFiveExpectedValues = new HashSet<>();
//        levelFourAndFiveExpectedValues.addAll(Arrays.asList("A_BBB", "B_BBB", "A_AAA", "B_AAA", "C_AAA"));
//
//        // LevelThreeC includes its own values for the Value element
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeC"));
//        Set<String> levelThreeInheritedValues = new HashSet<>();
//        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelThreeInheritedValues);
//
//        // LevelFourD contains Value elements and should have inherited ones
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFourD"));
//        Set<String> levelFourInheritedValues = new HashSet<>();
//        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelFourInheritedValues);
//
//        // LevelFive contains no Value elements but should inherited the Values in the preceding levels
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFive"));
//        Set<String> levelFiveInheritedValues = new HashSet<>();
//        model.getAdditiveInheritedField(MultipleEntryElement.NAME, levelFiveInheritedValues);
//
//        assertEquals("Only present value no inheritance", levelThreeExpectedValues, levelThreeInheritedValues);
//        assertEquals("Inherited with present values", levelFourAndFiveExpectedValues, levelFourInheritedValues);
//        assertEquals("Inherited with no own values", levelFourAndFiveExpectedValues, levelFiveInheritedValues);
//    }
//
//    @Test
//    public void testGetCharactersContent() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoB"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeB"));
//        String expectedValue = "Level Three B";
//
//        String actualValue = model.getCharactersContent();
//
//        assertEquals("Character Values", expectedValue, actualValue);
//    }
//
//    @Test
//    public void testGetChildCharacterContent() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoB"));
//        String expectedValue = "Level Three B";
//
//        String actualValue = model.getChildCharacterValue(new QName(NAMESPACE, "LevelThreeB"));
//
//        assertEquals("Character Values", expectedValue, actualValue);
//    }
//
//    @Test
//    public void testGetDoubleAttributeValue_NoInheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoA"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeA"));
//
//        double actualValue = model.getDoubleAttributeValue(new QName("", "levelthree"), false).doubleValue();
//
//        assertEquals("Not Inherited Double Attribute Value", DOUBLE_VALUE, actualValue, DELTA);
//    }
//
//    @Test
//    public void testGetDoubleAttributeValue_Inheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoB"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeB"));
//
//        double actualValue = model.getDoubleAttributeValue(new QName("", "leveltwo"), true).doubleValue();
//
//        assertEquals("Not Inherited Double Attribute Value", DOUBLE_VALUE, actualValue, DELTA);
//    }
//
//    @Test
//    public void testGetIntegerAttributeValue_NoInheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoA"));
//
//        int actualValue = model.getIntegerAttributeValue(new QName("", "leveltwo"), false).intValue();
//
//        assertEquals("Not Inherited Double Attribute Value", 2, actualValue);
//    }
//
//    @Test
//    public void testGetIntegerAttributeValue_Inheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoA"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeA"));
//
//        int actualValue = model.getIntegerAttributeValue(new QName("", "leveltwo"), true).intValue();
//
//        assertEquals("Not Inherited Double Attribute Value", 2, actualValue);
//    }
//
//    @Test
//    public void testGetBooleanAttributeValue_NoInheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoC"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelThreeC"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFourD"));
//        model = (XmlModel) model.getField(new QName(NAMESPACE, "LevelFive"));
//
//        boolean actualValue = model.getBooleanAttributeValue(new QName("", "levelfive"), false).booleanValue();
//
//        assertEquals("Not Inherited Boolean Attribute Value", true, actualValue);
//    }
//
//    @Test
//    public void testGetBooleanAttributeValue_Inheritance() {
//
//        // Get the leaf node
//        XmlModel model = (XmlModel) this.root.getField(new QName(NAMESPACE, "LevelTwoA"));
//
//        boolean actualValue = model.getBooleanAttributeValue(new QName("", "levelone"), true).booleanValue();
//
//        assertEquals("Not Inherited Double Attribute Value", false, actualValue);
//    }
}
