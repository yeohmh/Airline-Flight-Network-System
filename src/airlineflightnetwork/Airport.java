package airlineflightnetwork;

import java.util.Objects;

public class Airport {

    private final String code;
    private final String name;
    private final String city;
    private final String country;

    public Airport(
            String code,
            String name,
            String city,
            String country) {

        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.city = city.trim();
        this.country = country.trim();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return code + " - " + name
                + " (" + city + ", " + country + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Airport)) {
            return false;
        }

        Airport airport = (Airport) object;
        return code.equals(airport.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}