package com.redblueflame.library;
import com.redblueflame.importer.Experimental;
@Experimental("aaa")
public class LibraryTest {
    @Experimental("yyy")
    public int TestingField = 0;
    @Experimental("bbb")
    public InternalClass testing = null;
    @Experimental("ccc")
    public InternalEnum test_enum = InternalEnum.TESTS;
    @Experimental("xyz")
    public void TestingExperimental() {
        System.out.println("Testingg!");
        return;
    }
}
class InternalClass {
    public int other = 0;
}
enum InternalEnum {
    TESTS,
}
