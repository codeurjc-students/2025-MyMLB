import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ExportService {

	public downloadPNG(canvas: HTMLCanvasElement, fileName: string) {
		if (canvas) {
			const image = canvas.toDataURL('image/png', 1.0);
			const link = document.createElement('a');
			link.setAttribute('href', image);
			link.setAttribute('download', `${fileName}.png`);

			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
		}
	}
}