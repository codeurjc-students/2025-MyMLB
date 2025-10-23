module.exports = {
    darkMode: 'class',
    content: [
        './src/**/*.{html,ts}',         // tu código Angular
        './node_modules/flowbite/**/*.js' // 👈 esto permite que Tailwind escanee los componentes de Flowbite
    ],
    theme: {
        extend: {},
    },
    plugins: [
        require('flowbite/plugin') // 👈 esto activa los componentes de Flowbite
    ],
}