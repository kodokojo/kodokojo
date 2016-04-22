package io.kodokojo.service.redis;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisUtilsTest {

    @Test
    public void simple_aggregate_key() {

        byte[] expected = ("prefix/" + "macle").getBytes();
        byte[] result = RedisUtils.aggregateKey("prefix/", "macle");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void translate_10_to_0a() {
        String encode = RedisUtils.hexEncode(new byte[] {10});
        assertThat(encode).isEqualTo("0a");
    }
    @Test
    public void translate_11_to_0b() {
        String encode = RedisUtils.hexEncode(new byte[] {11});
        assertThat(encode).isEqualTo("0b");
    }
    @Test
    public void translate_12_to_0c() {
        String encode = RedisUtils.hexEncode(new byte[] {12});
        assertThat(encode).isEqualTo("0c");
    }
    @Test
    public void translate_13_to_0d() {
        String encode = RedisUtils.hexEncode(new byte[] {13});
        assertThat(encode).isEqualTo("0d");
    }
    @Test
    public void translate_14_to_0e() {
        String encode = RedisUtils.hexEncode(new byte[] {14});
        assertThat(encode).isEqualTo("0e");
    }
    @Test
    public void translate_15_to_0f() {
        String encode = RedisUtils.hexEncode(new byte[] {15});
        assertThat(encode).isEqualTo("0f");
    }

}