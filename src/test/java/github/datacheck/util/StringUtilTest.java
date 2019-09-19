package github.datacheck.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void underlineToHump() {
        Assert.assertEquals("userName",StringUtil.underlineToHump("user_name"));
        Assert.assertEquals("userName",StringUtil.underlineToHump("USER_NAME"));
    }

    @Test
    public void humpToLine() {
        Assert.assertEquals("user_name",StringUtil.humpToLine("userName"));
    }
}