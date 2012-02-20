package au.org.ala.checklist.lucene;

/**
 * An Enum for all the fields that are indexed for the name matching
 *
 * @author Natasha Carter
 */
public enum NameIndexField {
    ID("id"),
    LSID("lsid"),
    ACCEPTED("accepted_lsid"),
    GENUS_EX("genus_ex"), //genus sounds like expression - handles masculine and feminine too.
    SPECIES_EX("specific_ex"),// specific epithet sounds like expression
    INFRA_EX("infra_ex"),//infra specific epithet sounds like expression
    SPECIFIC("specific"),
    INFRA_SPECIFIC("infra"),
    NAME("name"),//canonical name
    RANK_ID("rank_id"),
    RANK("rank"),
    AUTHOR("author"),
    PHRASE("phrase"),//stores the values of a "phrase" name.  Some more intelligence will be needed when matching these
    VOUCHER("voucher"); //stores a voucher value minus the spaces and fullstops.
    String name;

    NameIndexField(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}