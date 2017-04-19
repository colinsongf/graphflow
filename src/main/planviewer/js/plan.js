/**
 * Submits the input query to the Graphflow server and render the result
 */
function submitQuery() {
    // empties the plan viewer
    $('#stageSet').empty();
    // obtains the query string
    var query = document.getElementById('query').value.trim();
    // sends the query to the Graphflow server
    $.post('http://localhost:8000/query', query).fail(function() {
        alert("Graphflow server is down!");
    });
    // renders the plans
    $.getJSON('http://localhost:8000/json', function(data, status, xhr) {
        renderPlan(data);
    }).fail(function() {
        $.get('http://localhost:8000/json', function(data, status, xhr) {
            $('#stageSet').append('<div class="center">Result: ' + data + '</div>');
        }).fail(function() {
            alert('Graphflow server is not responding!');
        });
    });
}

/**
 * Renders an execution plan
 */
function renderPlan(planArray) {
    var stageSet = '';

    var colSize = -1;
    if (3 >= planArray.length) {
        colSize = getColSize(planArray.length);
        stageSet += '<div class="row" id="planContainer"></div>';
    } else {
        stageSet += '<div id="planContainer"></div>';
    }
    $('#stageSet').append(stageSet);

    for (var k = 0; k < planArray.length; ++k) {
        stageSet = '';
        if (0 > colSize) {
            stageSet += '<div class="column">';
        } else {
            stageSet += '<div class="col s' + colSize + '">';
        }
        stageSet += '<div id="name' + k + '"></div>';
        stageSet += '<div id="ordering' + k + '"></div>';
        stageSet += '<div id="stages' + k + '"></div>';
        stageSet += '</div>';
        $('#planContainer').append(stageSet);
        renderPlanStages(planArray[k], k);
    }
}

/**
 * Renders a plan column at the {@code id}-th column position
 */
function renderPlanStages(plan, id) {
    var name = '<h3 class="center">' + plan.name + '</h3>';
    $('#name' + id).append(name);
    var ordering = '<h3 class="center">Variable Ordering:<br />' + plan.variableOrdering.join(', ') + '</h3>';
    $('#ordering' + id).append(ordering);

    var stages = plan.stages;
    var stageContainers = [];

    // creates HTML code snippet of initial stage
    var stage1Container = '<div id="stage1" class="center stage"><b>Scan:</b> <i>' + stages[0][0].variable + '</i> : ';
    stage1Container += getGraphVersionBox(stages[0][0].graphVersion);
    stage1Container += getDirectionSymbol(stages[0][0].direction) + '</div>';
    stageContainers.push(stage1Container);
    // renders the filter information if present
    var stage1Filter = getFilters(stages[0][0]);
    if (stage1Filter) {
        stage1Filter = '<div class="center stage"><h3 style="margin-bottom: 0px">Filter (&sigma;)</h3>' + stage1Filter;
        stage1Filter += '</div>';
        stageContainers.push(stage1Filter);
    }
    // creates HTML code snippet of the rest of stages
    for (i = 1; i < stages.length; ++i) {
        var intersectionRules = stages[i];
        var stageContainer = '<div class="center stage">';
        if (hasFilter(intersectionRules)) {
            stageContainer += '<h3>Intersection-And-Filter (&cap;&sigma;)</h3>';
        } else {
            stageContainer += '<h3>Intersection (&cap;)</h3>';
        }

        // creates HTML code snippet of all intersection rules of a stage
        for (var j = 0; j < intersectionRules.length; ++j) {
            if (0 != j) {
                stageContainer += '<div>&cap;</div>';
            }
            stageContainer += '<div class="center stage inner-stage"><i>' + intersectionRules[j].variable + '</i> : ';
            stageContainer += getGraphVersionBox(intersectionRules[j].graphVersion);
            stageContainer += getDirectionSymbol(intersectionRules[j].direction) + '<br />';
            stageContainer += getFilters(intersectionRules[j]) + '</div>';
        }

        stageContainer += '</div>';
        stageContainers.push(stageContainer);
    }

    var arrow = '<div class="center arrow"></div>';
    var arrowWithOutput = '<div class="center arrow"><div class="output">Output: <i>(';
    // creates HTML code snippet of operators
    var operators = plan.nextOperators;
    for (i = operators.length - 1; i >= 0; --i) {
        var operator = operators[i];
        var operatorDiv = '<div class="center stage">';

        if (operator.type) {
            operatorDiv += '<b>' + operator.type + '</b>: ' + operator.name;
        } else {
            operatorDiv += '<h3>' + operator.name + '</h3>';
        }

        var operatorArgs = operator.args;
        if (operatorArgs) {
            for (var j = 0; j < operatorArgs.length; ++j) {
                argValueArray = operatorArgs[j].value;
                if (typeof(argValueArray[0]) === 'object') {
                    operatorDiv += '<b>' + operatorArgs[j].name + ':</b><br />';
                    for (var k = 0; k < argValueArray.length; ++k) {
                        argValueString = '';
                        for (var field in argValueArray[k]) {
                            argValueString += '<b>' + field.charAt(0).toUpperCase() + field.slice(1) + '</b>=';
                            argValueString += argValueArray[k][field] + ' ';
                        }
                        operatorDiv += argValueString + '<br />';
                    }
                } else {
                    operatorDiv += '<b>' + operatorArgs[j].name + ':</b> ' + argValueArray.join(', ') + '<br />';
                }
            }
        }

        operatorDiv += '</div>';
        $('#stages' + id).append(operatorDiv);

        if (0 != i) {
            $('#stages' + id).append(arrow);
        }
    }
    // renders the plan stages with intermediate arrows and outputs
    var orderingArrayCopy = plan.variableOrdering.slice();
    for (i = stageContainers.length - 1; i >= 0; --i) {
        var finalOutput = arrowWithOutput + orderingArrayCopy.join(', ') + ')</i></div></div>';
        if (0 != i) {
            $('#stages' + id).append(finalOutput);
        } else {
            $('#stages' + id).append(arrow);
        }
        orderingArrayCopy.pop();
        $('#stages' + id).append(stageContainers[i]);
    }
}
