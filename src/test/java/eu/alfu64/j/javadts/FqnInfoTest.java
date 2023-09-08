package eu.alfu64.j.javadts;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FqnInfoTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void ofString() {
        FqnInfo fqnInfo=new FqnInfo("a.b.c.Classique");
        System.out.println(fqnInfo.toString());
    }
    @Test
    void ofStringArray() {
        FqnInfo fqnInfo=new FqnInfo("a.b.c.Classique[]");
        System.out.println(fqnInfo.toString());
    }
    @Test
    void ofStringBase() {
        FqnInfo fqnInfo=new FqnInfo("base.Classique");
        System.out.println(fqnInfo.toString());
    }
    @Test
    void ofStringRoot() {
        FqnInfo fqnInfo=new FqnInfo("Classique");
        System.out.println(fqnInfo.toString());
        System.out.println(fqnInfo.getPathToRoot());
        System.out.println(fqnInfo.getParentPath());
    }

    @Test
    void getPathTokens() {
    }

    @Test
    void getSimpleTypeName() {
    }

    @Test
    void getSimpleTypeNameNoArray() {
    }

    @Test
    void getParentFqn() {
    }

    @Test
    void getParentTokens() {
    }

    @Test
    void getPathToRoot() {
    }

    @Test
    void getParentPath() {
    }

    @Test
    void getParentFqnInfo() {
    }
}