package org.jasig.services.persondir.support.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.support.AbstractDefaultAttributePersonAttributeDao;

/**
 * Implementation of uPortal's <code>IPersonAttributeDao</code> that evaluates
 * person directory information based on configurable rules.  You may chain as 
 * many rules as you like, but this DAO will apply <b>at most</b> one rule, the
 * first that triggers.
 * 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">rules</td>
 *         <td>
 *             The array of {@link AttributeRule}s to use when 
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 */
public final class DeclaredRulePersonAttributeDao extends AbstractDefaultAttributePersonAttributeDao {

    /**
     * List of {@link AttributeRule} objects.
     */
    private List<AttributeRule> rules;


    /**
     * Creates a new DeclaredRulePersonAttributeDao specifying the attributeName to pass to
     * {@link #setDefaultAttributeName(String)} and the {@link List} of {@link AttributeRule}s
     * to pass to {@link #setRules(AttributeRule[])}
     * 
     * @param attributeName
     * @param rules
     */
    public DeclaredRulePersonAttributeDao(String attributeName, List<AttributeRule> rules) {
        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Creating DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }

        // Instance Members.
        this.setDefaultAttributeName(attributeName);
        this.setRules(rules);

        // PersonDirectory won't stop for anything... we need decent logging.
        if (logger.isDebugEnabled()) {
            logger.debug("Created DeclaredRulePersonAttributeDao with attributeName='" + attributeName + "' and rules='" + rules + "'");
        }
    }
    
    /**
     * @return the rules
     */
    public List<AttributeRule> getRules() {
        return this.rules;
    }
    /**
     * @param rules the rules to set
     */
    public void setRules(List<AttributeRule> rules) {
        Validate.notEmpty(rules, "Argument 'rules' cannot be null or empty.");

        this.rules = Collections.unmodifiableList(new ArrayList<AttributeRule>(rules));
    }


    /*
     * @see org.jasig.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map<String, List<Object>> getUserAttributes(final Map<String, List<Object>> seed) {
        Validate.notNull(seed, "Argument 'seed' cannot be null.");

        Map<String, List<Object>> rslt = null;    // default (contract of IPersonAttributeDao)

        for (final AttributeRule rule : this.rules) {
            if (rule.appliesTo(seed)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Evaluating rule='" + rule + "' from the rules List");
                }

            	rslt = rule.evaluate(seed);
                break;  // We promise to apply at most one rule...
            }
        }

        return rslt;
    }

    /**
     * Aggregates the results of calling {@link AttributeRule#getPossibleUserAttributeNames()}
     * on each {@link AttributeRule} instance in the rules array.
     * 
     * @see org.jasig.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set<String> getPossibleUserAttributeNames() {
        Set<String> rslt = new HashSet<String>();

        for (final AttributeRule rule : this.rules) {
            final Set<String> possibleUserAttributeNames = rule.getPossibleUserAttributeNames();
            rslt.addAll(possibleUserAttributeNames);
        }

        return rslt;
    }

}