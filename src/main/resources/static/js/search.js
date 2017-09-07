$(function () {
    var wrapper = $(".name_group");
    var add_button = $("#add_button");
    var add_multiple_button = $("#add_multiple_button");

    var x = 1;

    var append_movie_input = function () {
        x++;
        $(wrapper).append('<div class="form-group col-lg-4 row" id="name_group' + x + '">\
                    <label class="col-sm-2 control-label">电影:</label>\
            <div class="col-sm-6">\
                    <input type="text" class="form-control"\
            placeholder="请输入电影名称"><input type="number" class="form-control"\
        placeholder="请输入年份"><button  class="remove_field btn" value="' + x + '">删除</button>\
                    </div>\
                    </div>\
                    ');
    };

    $(add_button).click(function (e) {
        e.preventDefault();
        append_movie_input();
    });

    $(wrapper).on("click", ".remove_field", function (e) {
        e.preventDefault();
        $('#name_group' + $(this).val()).remove();
        this.remove();
    });

    add_multiple_button.on('click', function (e) {
        e.preventDefault();
        $.confirm({
            title: '请输入需要添加的个数',
            content: getInputHtml(),
            buttons: {
                confirm: {
                    text: '添加',
                    btnClass: 'btn-success',
                    action: function () {
                        var $movie_number = this.$content.find('input[name=movie_number]');
                        var val = parseInt($movie_number.val());
                        if (!isNaN(val) && val > 0) {
                            for (var i = 0; i < val; i++) {
                                append_movie_input();
                            }
                        }
                        return true;
                    }
                },
                cancel: {
                    text: '取消'
                }
            }
        });
    });

    function getInputHtml() {
        return [
            '<div class="input-group">',
            '<input type="number" name="movie_number" min="0" width="auto" value="0" type="number" class="form-control text-right">',
            '<span class="input-group-addon">个</span>',
            '</div>'].join('');
    }

    $("#movie_form").on("submit", function () {
        $('#query').val(buildQuery());
        $('.form-control').attr('disabled', true);
    });

    var buildQuery = function () {
        var inputs = $('.form-control');
        var content = [];
        var count = 0;
        var tmp;
        inputs.each(function () {
            if ((++count) % 2 === 1) {
                tmp = $(this).val().trim() || '...';
            } else {
                tmp += '_' + ($(this).val().trim() || '...');
                if (tmp.indexOf('...') === -1) {
                    content.push(tmp);
                }
            }
        });
        return JSON.stringify(content);
    }
});