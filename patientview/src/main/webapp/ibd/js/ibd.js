var IBD = {};

IBD.AddMedicineInit = function() {
    var medicationType              = $('#medicationTypeId'),
        medicationContainer         = $('#medicationContainer'),
        medicationListsContainer    = $('#medicationLists'),
        medicationLists             = medicationListsContainer.find('.medicationList'),
        medicationDoseContainer     = $('#medicationDoseContainer'),
        medicationDoseLists         = medicationDoseContainer.find('.medicationDoseList'),
        medicationOtherContainer    = $('#medicationOtherContainer'),
        otherMedicationInput        = $('#otherMedication'),
        disableLists                = function(list) {
            if (list) {
                list.each(function(i, el) {
                    $(el).addClass('hidden')[0].disabled = true;
                });
            }
        },
        enableList                  = function(list) {
            if (list) {
                list.removeClass('hidden')[0].disabled = false;
            }
        };

    medicationType.on('change', function() {
        var medicationTypeId = parseInt(medicationType.find('option:selected').val());

        // if a value is selected then show the corresponding list of medications else hide if they are showing
        if (medicationTypeId !== NaN && medicationTypeId > 0) {
            medicationContainer.removeClass('hidden');
            disableLists(medicationLists);
            enableList($('#medicationType' + medicationTypeId + '-medications'));
        } else {
            medicationContainer.addClass('hidden');
            disableLists(medicationLists)
        }
    });

    medicationLists.on('change', function(el) {
        var medicationId = parseInt($(this).find('option:selected').val());

        // if a value is selected then show the corresponding list of dosages else hide if they are showing
        // if the value is -2 then this is the 'other' option been selected in which case show the other text field
        if (medicationId === NaN || medicationId <= 0) {
            medicationDoseContainer.addClass('hidden');
            disableLists(medicationDoseLists);

            if (medicationId !== NaN && medicationId === -2) {
                medicationOtherContainer.removeClass('hidden');
            }
        } else {
            otherMedicationInput.val('');
            medicationOtherContainer.addClass('hidden');
            medicationDoseContainer.removeClass('hidden');
            disableLists(medicationDoseLists);
            enableList($('#medication' + medicationId + '-dosages'));
        }
    })
};

$(function() {
    $('.datePicker').datepicker({
        format:'dd-mm-yyyy'
    }).on('changeDate', function (ev) {
            $(ev.currentTarget).datepicker('hide')
    });

    if ($('#addMedication').length > 0) {
        IBD.AddMedicineInit();
    }
});