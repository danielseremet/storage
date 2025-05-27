import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IStorageFile } from '../storage-file.model';

@Component({
  selector: 'jhi-storage-file-detail',
  templateUrl: './storage-file-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class StorageFileDetailComponent {
  storageFile = input<IStorageFile | null>(null);

  previousState(): void {
    window.history.back();
  }
}
