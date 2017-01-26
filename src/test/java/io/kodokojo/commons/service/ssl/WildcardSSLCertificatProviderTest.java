/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.service.ssl;

import static org.assertj.core.api.Assertions.assertThat;

public class WildcardSSLCertificatProviderTest {
/*
    @Test
    public void extractContent() {
        try {
            String content = IOUtils.toString(new FileReader(new File("src/test/resources/keystore/rootCa.pem")));
            String privateKey = WildcardSSLCertificatProvider.extractPrivateKey(content);
            String publicKey = WildcardSSLCertificatProvider.extractPublic(content);
            assertThat(privateKey).isEqualTo("-----BEGIN RSA PRIVATE KEY-----\n" +
                    "MIIJKQIBAAKCAgEA1+sxHqNJm1g+XJ59E90a8vunVQvQobYSmvRQy54TzvmPNvEX\n" +
                    "1ASYnok6E3CwMdxE7M4QFBmsJCVYrxawwXrVqO+Wwy02ZtwR4AYz3rgrrGtCFw32\n" +
                    "A8GALCJsaiXjiMoo217jIVvYM0dg8R7fdAv1SRA+F+avjm/zux+HSZdgPJN+Mgbu\n" +
                    "4fSiafw4vnkn9dX00sHihYwl7rLnPEpPackkX6MhfvCUXpg/Y33qY9Rfs+n09nRr\n" +
                    "BAN6v9R+bVnECXBh80B4BoyzjHOcPasEOZNxqdL7q8AcuUsz0Gf5plPNiwFrf7fM\n" +
                    "I/C2D02mivo8hKyXEqSsXSg/vW/SY6TdeyU+AzJVkB7DdJnF2jMZf+KHi2unHpZg\n" +
                    "DHpTNBPXGDA3mm9D4szglZbi+xqjJJ5hAY1m3Ov5fab0b+HGde1dMCXOhQKd4fbD\n" +
                    "JDkyLa6cs1KZLmuyFhnef8oO0+5y2N1vs7yaA9mLG6mAh3j2ANiO4wCDeq1cQcui\n" +
                    "v8+HKTk1dCqt6W0yd7HMyjLX05Phji/UvWJT+Lbd8cv7Gb3XREG0TY6uqo2WQQsS\n" +
                    "ZjKqznw+Aj8saMgBhwQP7CctT4i8iP6mGlxiB4OcgRfQaUZQM6XjQhQg236jid3v\n" +
                    "iwCFUFT8G0wGJqsk28WxvWy0gGvdw/rQPog3Ac+wkFlhQiNCUdzfsH3R7zsCAwEA\n" +
                    "AQKCAgEArlc8eSWViHZPHE/oZIKSgQHLkfMRSZGPIkkFd/OywNtm1REEkXAIn3Hk\n" +
                    "T9AcpJXbnW2hM+mqwiDZo75piEAbhuW10PCPzawCTuYTOdFxii2s1pqyO0QhrwE4\n" +
                    "ktj3CPta1A6gzM2xG26WCvtHqBcDvjP8PyZIOHWS0lPZ1AUDcsYRDmnkttPl1DgQ\n" +
                    "xVNkfa59mTNuKTEJa9xOs7K4vozn7sT0hd/RnTPHcxk3idXwVIAjCGDmC/6XPY9h\n" +
                    "agAiNxDkNhqnD3rKeyzaSjztE2RHUb2G8p4TU1YyhyN9Q9zEyWmjGCQbFCYHiwHC\n" +
                    "6NK2zNXiwe3vMuUN2nMQ6N4063sdvtFXiTf6MW8lA7RcRzk8HbgVEkjIvcYX6vDe\n" +
                    "FwNmWqDb/MVmftY3ZNbnMIoxwulsUrMxrMfg211vBW73JkPt3m/9HyNypO9OmCqG\n" +
                    "eoUJIaWI06vUGP3FreJZtklUhQ1nnFhJ0K8hux2I5HpDfVb0x6RCk3cujsjsxUsz\n" +
                    "Z4wLoP4Dqo6pl2KyVuXAtGzuo0jxtj8vCJKvP13FIbyw9PZcs9dO4uRoQqglY/fB\n" +
                    "riiY9SFD5vzXT0vXgJWWOYs70DtjokMBSlGyqshvYmBVON/52Ru3DDKijMvWfqQ/\n" +
                    "PQLeRSxHRYAVpChU/Tu1qaAqA4gDdbs1dBFLPgLE1R98qDDzHskCggEBAOv2eU5x\n" +
                    "hEc2I9V2SdS1FwVAvpzL0Ha3lKuDkpD6HE0UVDVyofC3zb8h8VF1ugMIeGhSyZ37\n" +
                    "E7nrNpC1tly9rl52yWCu0OXfGoDf3AVDiaRBhw7mkgNL3hQTveqKpMge8nXe78or\n" +
                    "ev0RI+IuhBb7vRebuGMS+G46rxRJ27MvP2nve8EEQwKjYUUvk+dtPJjP1KvaOVM5\n" +
                    "itL7nM0esBGzC0n66nk7LkQ+fbNDmULcTBK/lDTDKaKbyOxKAmUHnz7JaXMgAljN\n" +
                    "ZTCh754NeOxDjSXKhkbG8XOlUXOJbBwwVQ7qh5JZXmk9leT4NqfkZ4C5BbP4SBOM\n" +
                    "Z3dVzY30fH3OP30CggEBAOpA/F91NrhTkK6/YirGQ4Ss9z4rMvLsh4AVmWHGTfe8\n" +
                    "emChfzrMVl2g2gvzhcITDvD+FoKKBv6/gib0//IoN7O1UOeduw1faeyOKoJX96MX\n" +
                    "XShQwr7AdzqtyJmkzmP7sNeyJWoLHybP6569kT88SbNx+4If0FKgwTiGPaPxfP/C\n" +
                    "waTW4GygAuclETXz6JGlFIidtnDy5L+7VdzOTkhkp2L7I7L9AsMoo7fZbnStDAMH\n" +
                    "hEhYGBPsDYf3Fu684RINzKna9KqtwLubBuC2y0YRZ01n2DiwHPnZXFj3LgXaC/rm\n" +
                    "l9XsJPNoQoILf3M10g5QPDwFTZOBTzGlMvM113rOFxcCggEBALgLVdCeb+tPSiHc\n" +
                    "EngrCxqYxlPsENwYEoi4piAYROzF92x8zdzAsGTtJ5k+9ugYujm8oO9s/5Ta5oN2\n" +
                    "iBfMBu49PpL/p4Y0PyrX8HfWlswcJCi2JJsXmM8WGuKOYzqIENtpGKUaxuIOdWuc\n" +
                    "loCKt5FrbyUvjcAEW7y6Yvde2dpZ5a/GEDJFlr/s/TLcHBCgwiOMoXvNfP9VK5c9\n" +
                    "4VzcxuhcIniOdWbbSxYAmtDwnRFehk2lD5t6YPwUOcvtZwME/A/Enq/+/HczR3+C\n" +
                    "yJeXlMcuMQ+RapCmGGLChCV0eVOaEEZRGHeDhp/CDndjimt3ZQBbnnRI5gbJUPvw\n" +
                    "/wztSs0CggEAZbDkXgQiRw5vxoxqbys2cEgt5J0YNwCbKe02co2Fl0AI2B4Df2Dp\n" +
                    "NSf4wCttK3zehZun5e9JbhlwxLyix1rSI39YqdN9OibrcrecmIzbgb9Iv5GfmPMw\n" +
                    "TxbV+Gzk8951PI2nVEuTsGOeY4DjptHjJLUce7aX1ubWOlV8OMlCfDztl0N4lW85\n" +
                    "E2Kx//vsQB2rtkrQwmOPDfSjJ4Gf1ilryLpBGxhetJ3VxJ/tJVVh24bN1CH6Adv9\n" +
                    "W8LV/7ma8x1udWYJxHY9LrPz3mU8BSTU9XNZF/LMAWJshmp3Xi7z+G1WCJXEzK28\n" +
                    "AXxATaFVmMUYAO5EaP4/hMu0PIRpEHSa7QKCAQB27JiYFTAke4oqVf6rq+dqEPXy\n" +
                    "Tpr5x0zufyWLHVjASFPURjrdYKBWG0z0kgsyJcT1SOAzU/5SuBZIvJaGfUS4NE+Q\n" +
                    "d8Cv5OGkxKt4CjOj5kotxzyWLop458WO3tBs2B/Z+DT3FKOPwPTtCe+GCDg3f+Tb\n" +
                    "mdH5byhHjVTr+y/dSpyXRGmBUikFk5e6boDeWJZa/spqruu1zlTT1hpvhvBK1L/F\n" +
                    "fyMPSdyD8Cb8HjEFSsiy43ltP9Xt6BG2S0E2nfYJTgi9pwL14BFC/2ZXyK/Yp2cD\n" +
                    "VyE/JpH6lE7ScQw7kRg0DVbENBJ+/A8iW86h3P+CD/i+dCqU837HyH7QHEFv\n" +
                    "-----END RSA PRIVATE KEY-----");
            assertThat(publicKey).isEqualTo("-----BEGIN CERTIFICATE-----\n" +
                    "MIIGPzCCBCegAwIBAgIJAPNi59csOsXaMA0GCSqGSIb3DQEBCwUAMHIxCzAJBgNV\n" +
                    "BAYTAkZSMQ8wDQYDVQQIEwZGcmFuY2UxDjAMBgNVBAcTBVBhcmlzMRIwEAYDVQQK\n" +
                    "EwlLb2RvIEtvam8xDTALBgNVBAsTBFRlc3QxHzAdBgNVBAMTFkZha2UgUm9vdCBD\n" +
                    "QSBLb2RvIEtvam8wHhcNMTYwMzE4MTM0ODAyWhcNMzYwMzEzMTM0ODAyWjByMQsw\n" +
                    "CQYDVQQGEwJGUjEPMA0GA1UECBMGRnJhbmNlMQ4wDAYDVQQHEwVQYXJpczESMBAG\n" +
                    "A1UEChMJS29kbyBLb2pvMQ0wCwYDVQQLEwRUZXN0MR8wHQYDVQQDExZGYWtlIFJv\n" +
                    "b3QgQ0EgS29kbyBLb2pvMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA\n" +
                    "1+sxHqNJm1g+XJ59E90a8vunVQvQobYSmvRQy54TzvmPNvEX1ASYnok6E3CwMdxE\n" +
                    "7M4QFBmsJCVYrxawwXrVqO+Wwy02ZtwR4AYz3rgrrGtCFw32A8GALCJsaiXjiMoo\n" +
                    "217jIVvYM0dg8R7fdAv1SRA+F+avjm/zux+HSZdgPJN+Mgbu4fSiafw4vnkn9dX0\n" +
                    "0sHihYwl7rLnPEpPackkX6MhfvCUXpg/Y33qY9Rfs+n09nRrBAN6v9R+bVnECXBh\n" +
                    "80B4BoyzjHOcPasEOZNxqdL7q8AcuUsz0Gf5plPNiwFrf7fMI/C2D02mivo8hKyX\n" +
                    "EqSsXSg/vW/SY6TdeyU+AzJVkB7DdJnF2jMZf+KHi2unHpZgDHpTNBPXGDA3mm9D\n" +
                    "4szglZbi+xqjJJ5hAY1m3Ov5fab0b+HGde1dMCXOhQKd4fbDJDkyLa6cs1KZLmuy\n" +
                    "Fhnef8oO0+5y2N1vs7yaA9mLG6mAh3j2ANiO4wCDeq1cQcuiv8+HKTk1dCqt6W0y\n" +
                    "d7HMyjLX05Phji/UvWJT+Lbd8cv7Gb3XREG0TY6uqo2WQQsSZjKqznw+Aj8saMgB\n" +
                    "hwQP7CctT4i8iP6mGlxiB4OcgRfQaUZQM6XjQhQg236jid3viwCFUFT8G0wGJqsk\n" +
                    "28WxvWy0gGvdw/rQPog3Ac+wkFlhQiNCUdzfsH3R7zsCAwEAAaOB1zCB1DAdBgNV\n" +
                    "HQ4EFgQUB+LTINZCJHgY0Jxe3dCBgWqCb48wgaQGA1UdIwSBnDCBmYAUB+LTINZC\n" +
                    "JHgY0Jxe3dCBgWqCb4+hdqR0MHIxCzAJBgNVBAYTAkZSMQ8wDQYDVQQIEwZGcmFu\n" +
                    "Y2UxDjAMBgNVBAcTBVBhcmlzMRIwEAYDVQQKEwlLb2RvIEtvam8xDTALBgNVBAsT\n" +
                    "BFRlc3QxHzAdBgNVBAMTFkZha2UgUm9vdCBDQSBLb2RvIEtvam+CCQDzYufXLDrF\n" +
                    "2jAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4ICAQCbGb+gIzR8BcSKi7r7\n" +
                    "LsVSS3cD5ERPS5bmr3Dl+ZB89o8XSAlwzdZsz34zFgY+Y9z2VexpOKL5C+1IQArp\n" +
                    "uDm0MOwuYPZiSBN7gkwDSEIuCME8bA/fsB5Tkj4AsOu32TXWjnzTlcr8KboTAhSS\n" +
                    "stRXeHcBifATSQYK9JM2xxmzHJ80Fi+CK/DaLZdl1vT6OridOu0m+i7wQltBzfJ0\n" +
                    "m+nlEMwt8GOYo58ta0py64YbST2NJz48W8Jupw32jzrx0G3LMC7eJYbf7CcS31FV\n" +
                    "+nojkZ0xytLstgM5fPphrJNYA6Pp3fJrtFrSA3S/iJWgYVKJm5cKaK0AP0Jt9B29\n" +
                    "mcwCTRJouPQ1WshSPfyNSQYwS9Ps5Zrmx6+7gcLUDeEs7ExUt+5heEXZG1mwPRo8\n" +
                    "8EpkXfBYvYH4e4DadayCDoLwqSzSF+YixTJCYgCuNWxPgRpnbYQFmYSyscKCNEpV\n" +
                    "RHIcT+ah8d93498DFCjhHJ4hNOYwT8OI37maxj+5wG+toqzckT3+NM0Ge8rv4BrX\n" +
                    "qKliTpFcXC8EpukWn0Esjn0TTHIVVdID6xlK1QwGsutOVV3eE5Ff9XaIrAsYVEg2\n" +
                    "93oft3Y7umiUisSFsJAMI8jGK6SjPAOSmdFYAy27ETqonvxsOT3DG64PnvC3q+YE\n" +
                    "Yif8hHxLqRKVNlBm4+EFa5s8Ig==\n" +
                    "-----END CERTIFICATE-----");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void loadPemFile() {
        WildcardSSLCertificatProvider wildcardSSLCertificatProvider = new WildcardSSLCertificatProvider(new File("src/test/resources/keystore/rootCa.pem"));
        SSLKeyPair sslKeyPair = wildcardSSLCertificatProvider.provideCertificat("toto", "tata", null);
        assertThat(sslKeyPair).isNotNull();
        assertThat(sslKeyPair.getPrivateKey()).isNotNull();
        assertThat(sslKeyPair.getPublicKey()).isNotNull();
    }
    */

}