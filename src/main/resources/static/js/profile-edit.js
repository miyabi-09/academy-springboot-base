document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('image');
    const out   = document.getElementById('file-name');
    if (!input || !out) return;

    input.addEventListener('change', function () {
        const name = (this.files && this.files.length) ? this.files[0].name : '未選択';
        out.textContent = name.replace(/^.*[\\\/]/, '');
    });
});
