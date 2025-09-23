export default function (config) {
	config.set({
		basePath: '',
		frameworks: ['jasmine', '@angular-devkit/build-angular'],
		plugins: [
			require('karma-jasmine'),
			require('karma-chrome-launcher'),
			require('karma-jasmine-html-reporter'),
			require('karma-coverage'),
			require('@angular-devkit/build-angular/plugins/karma')
		],
		client: {
			clearContext: false
		},
		reporters: ['progress', 'kjhtml'],
		customLaunchers: {
			ChromeHeadlessCI: {
				base: 'ChromeHeadless',
				flags: [
					'--no-sandbox',
					'--disable-gpu',
					'--disable-dev-shm-usage',
					'--disable-software-rasterizer',
					'--disable-extensions'
				]
			}
		},
		browsers: process.env.CI ? ['ChromeHeadlessCI'] : ['Chrome'], // In CI dont show any IU, in Local yes
		restartOnFileChange: true,
		proxies: {
			'/api/': 'http://localhost:8080/api/'
		},
	});
};