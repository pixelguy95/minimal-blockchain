package apis.utils.validators;

import domain.Validatable;

public interface Validator {

    Result validate(Validatable v);

    class Result {
        public String resaon = "";
        public boolean passed = true;

        public Result(String resaon) {
            passed = false;
            this.resaon = resaon;
        }

        public Result() {
        }
    }
}
