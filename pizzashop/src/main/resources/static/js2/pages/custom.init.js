/**
 * custom.init.js - Phiên bản tối ưu để hiển thị biểu đồ doanh thu (Sales Analytics)
 * Tự động lấy dữ liệu từ backend (Thymeleaf) qua biến monthlyDataJson
 * và vẽ biểu đồ bằng MorrisJS.
 */

(function ($) {
    "use strict";

    // ===============================
    // Constructor
    // ===============================
    var CustomDashboard = function () {};

    /**
     * Vẽ biểu đồ dạng line chart (doanh thu hoàn thành & hủy)
     * @param elementId {string} ID của phần tử chứa biểu đồ
     * @param data {Array} Dữ liệu đầu vào (month, completed, cancelled)
     */
    CustomDashboard.prototype.createLineChart = function (elementId, data) {
        if (!document.getElementById(elementId)) {
            console.warn(`⚠️ Không tìm thấy #${elementId}, bỏ qua.`);
            return;
        }

        if (!data || data.length === 0) {
            console.warn(`⚠️ Không có dữ liệu để vẽ biểu đồ ${elementId}`);
            return;
        }

        Morris.Line({
            element: elementId,
            data: data,
            xkey: 'month',
            ykeys: ['completed', 'cancelled'],
            labels: ['Completed', 'Cancelled'],
            lineColors: ['#28a745', '#dc3545'],
            lineWidth: 3,
            pointSize: 5,
            fillOpacity: 0.2,
            behaveLikeLine: true,
            hideHover: 'auto',
            resize: true,
            gridLineColor: "rgba(108, 120, 151, 0.1)"
        });
    };

    /**
     * Khởi tạo dashboard
     */
    CustomDashboard.prototype.init = function () {
        console.log("✅ custom.init.js đang chạy...");

        // Lấy dữ liệu từ Thymeleaf (Spring backend)
        let rawData = '[]';
        if (typeof monthlyDataJson !== 'undefined') {
            rawData = monthlyDataJson;
        } else if (typeof window.monthlyDataJson !== 'undefined') {
            rawData = window.monthlyDataJson;
        }

        let parsedData = [];
        try {
            parsedData = JSON.parse(rawData);
        } catch (err) {
            console.error("❌ Lỗi parse JSON từ backend:", err);
            parsedData = [];
        }

        console.log("📊 Dữ liệu backend:", parsedData);

        if (!Array.isArray(parsedData) || parsedData.length === 0) {
            console.warn("⚠️ Không có dữ liệu hợp lệ từ backend để vẽ biểu đồ.");
            return;
        }

        // Chuẩn hóa dữ liệu để vẽ
        const chartData = parsedData.map(item => ({
            month: item.month || '',
            completed: item.completedTotal || 0,
            cancelled: item.cancelledTotal || 0
        }));

        // Vẽ biểu đồ doanh thu
        this.createLineChart("sales-line-chart", chartData);
    };

    // ===============================
    // Gán prototype
    // ===============================
    $.CustomDashboard = new CustomDashboard();
    $.CustomDashboard.Constructor = CustomDashboard;

})(window.jQuery);


// ===============================
// Auto-run sau khi DOM load xong
// ===============================
(function ($) {
    "use strict";

    $(function () {
        if ($.CustomDashboard && $.CustomDashboard.init) {
            $.CustomDashboard.init();
        } else {
            console.warn("⚠️ CustomDashboard chưa được khởi tạo!");
        }
    });

})(window.jQuery);
