package airlineflightnetwork;

import java.util.Locale;
import java.util.Objects;

public class Airport {
    private final String code;
    private final String name;
    private final String city;
    private final String country;

    public Airport(String code, String name, String city, String country) {
        this.code = requireText(code, "Airport code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "Airport name");
        this.city = requireText(city, "City");
        this.country = requireText(country, "Country");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        return value.trim();
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }

    @Override
    public String toString() {
        return code + " - " + name + " (" + city + ", " + country + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Airport)) return false;
        Airport other = (Airport) object;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }
}
