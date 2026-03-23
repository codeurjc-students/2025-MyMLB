import { Injectable } from '@angular/core';
import JSZip from 'jszip';

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

	async downloadZip(zipFile: JSZip, date: string) {
		const zipContent = await zipFile.generateAsync({ type: 'blob' });
		const link = document.createElement('a');
		link.href = URL.createObjectURL(zipContent);
		link.download = `API_Analytics_${date}.zip`;
		link.click();
	}
}