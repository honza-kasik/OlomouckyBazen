package cz.honzakasik.bazenolomouc.pool;

import java.util.Date;

/**
 * Interface for providers of swimming pool
 */
public interface SwimmingPoolProvider {

    SwimmingPool obtainSwimmingPoolForDatetime(Date date);

}
