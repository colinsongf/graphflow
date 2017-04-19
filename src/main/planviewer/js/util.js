/**
 * Determines if at least one element of {@code intersectionRules}
 * has a filter type
 *
 * @return true if {@code intersectionRules} has a filter type, false otherwise
 * @param {Object[]} array of intersection rules
 */
function hasFilter(intersectionRules) {
    for (var i = 0; i < intersectionRules.length; ++i) {
        if (intersectionRules[i].hasOwnProperty(FILTER_TYPES.EDGE_TYPE) ||
            intersectionRules[i].hasOwnProperty(FILTER_TYPES.FROM_VERTEX_TYPE) ||
            intersectionRules[i].hasOwnProperty(FILTER_TYPES.TO_VERTEX_TYPE)) {
            return true;
        }
    }
    return false;
}

function getFilters(intersectionRule) {
    var hasFilter = false;
    var filter = '<b>&sigma;:</b>'
    if (intersectionRule.hasOwnProperty(FILTER_TYPES.EDGE_TYPE)) {
        filter += ' edgeType=' + intersectionRule.edgeType;
        hasFilter = true;
    }
    if (intersectionRule.hasOwnProperty(FILTER_TYPES.FROM_VERTEX_TYPE)) {
        filter += ' fromVertexType=' + intersectionRule.fromVertexType;
        hasFilter = true;
    }
    if (intersectionRule.hasOwnProperty(FILTER_TYPES.TO_VERTEX_TYPE)) {
        filter += ' toVertexType=' + intersectionRule.toVertexType;
        hasFilter = true;
    }

    if (hasFilter) {
        return filter;
    } else {
        return '';
    }
}

/**
 * Creates the HTML code of the symbol indicating the graph version
 *
 * @return a string of HTML code for the symbol
 * @param {string} the graph version
 */
function getGraphVersionBox(graphVersion) {
    result = '<div class="graph-version-box '
    switch (graphVersion) {
        case GRAPH_VERSIONS.NEW_GRAPH:
            result += 'new-graph">' + SYMBOLS[GRAPH_VERSIONS.NEW_GRAPH];
            break;
        case GRAPH_VERSIONS.CURRENT_GRAPH:
            result += 'current-graph">' + SYMBOLS[GRAPH_VERSIONS.CURRENT_GRAPH];
            break;
        case GRAPH_VERSIONS.DELTA_GRAPH_MINUS:
            result += 'delta-graph">' + SYMBOLS[GRAPH_VERSIONS.DELTA_GRAPH_MINUS];
            break;
        case GRAPH_VERSIONS.DELTA_GRAPH_PLUS:
            result += 'delta-graph">' + SYMBOLS[GRAPH_VERSIONS.DELTA_GRAPH_PLUS];
            break;
        default:
            return '';
    }
    result += '</div>';
    return result;
}

/**
 * Transforms {@code direction} into an arrow indicating the direction
 *
 * @return a string of an arrow
 * @param {string} the text indicating the direction
 */
function getDirectionSymbol(direction) {
    switch (direction) {
        case EDGE_DIRECTIONS.OUTGOING:
            return ASCII_ARROW[EDGE_DIRECTIONS.OUTGOING];
        case EDGE_DIRECTIONS.INCOMING:
            return ASCII_ARROW[EDGE_DIRECTIONS.INCOMING];
        default:
            return '';
    }
}

/**
 * Determines the size of the plan columns based on the number of columns
 *
 * @return the size of a column
 * @param {integer} the number of plan columns
 */
function getColSize(numSets) {
    if (3 === numSets) {
        return 3;
    } else if (3 > numSets) {
        return 12 / numSets;
    } else {
        return -1;
    }
}
