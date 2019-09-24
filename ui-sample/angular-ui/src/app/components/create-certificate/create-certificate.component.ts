import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { DataService } from '../../services/data/data.service';
import { ResourceService } from '../../services/resource/resource.service';
import { FormService } from '../../services/forms/form.service';
import { DefaultTemplateComponent } from '../default-template/default-template.component';
import * as _ from 'lodash-es';
import urlConfig from '../../services/urlConfig.json';
import { CertReq } from '../../services/interfaces/certificate';
import { Router } from '@angular/router';



@Component({
  selector: 'app-create-certificate',
  templateUrl: './create-certificate.component.html',
  styleUrls: ['./create-certificate.component.scss']
})
export class CreateCertificateComponent implements OnInit {

  @ViewChild('formData') formData: DefaultTemplateComponent;
  @ViewChildren('signatoryForm') signatoryFormData: QueryList<DefaultTemplateComponent>;
  dataService: DataService;
  formService: FormService;
  resourceService: ResourceService;
  pdfUrl: String;
  public formFieldProperties: any;
  public issuerFieldProperties: any;
  public signatoryFieldProperties: any;
  public req: CertReq;
  router: Router
  signatory: Array<Number> = [1];
  signatoryCount = 1;
  constructor(dataService: DataService, formService: FormService, resourceService: ResourceService, router: Router) {
    this.dataService = dataService;
    this.resourceService = resourceService;
    this.formService = formService;
    this.router = router;
  }

  ngOnInit() {
    this.formService.getFormConfig("certificate").subscribe(res => {
      this.formFieldProperties = res.fields;
    });
    this.formService.getFormConfig("signatory").subscribe(res => {
      this.signatoryFieldProperties = res.fields;
    });

  }
  createCertificate() {
    console.log(this.formData.formInputData)
    const certificateData = this.generateData(_.pickBy(this.formData.formInputData));
    const requestData = {
      data: {
        params: {},
        request: {
          certificate: certificateData
        }
      },
      url: urlConfig.URLS.GENERTATE_CERT
    }
    console.log("form data", requestData)
    this.dataService.post(requestData).subscribe(res => {
      console.log('certificate generated successfully', res)
      if(res.result.response[0].preview)  {
        window.open(res.result.response[0].pdfUrl, '_blank');
      } else  {
        this.pdfUrl = res.result.response[0].pdfUrl;
        this.dowloadPdf();

      }
    });
  }
  generateData(request: any) {
    const signatoryList = [];
    const requestData = _.cloneDeep(request);
    var certificate = {
      data: [],
      issuer: {},
      signatoryList: [],
      htmlTemplate: '',
      courseName: '',
      name: '',
      description: '',
    };
    const data = [{
      recipientName: requestData.recipientName,
      recipientEmail: requestData.recipientEmail,
      recipientPhone: requestData.recipientPhone,
    }];
    const issuer = {
      name: requestData.name,
      url: requestData.url,
    }
    this.signatoryFormData.forEach(issuer => {
      signatoryList.push(issuer.formInputData);
    });
    certificate.data = data;
    certificate.issuer = issuer;
    certificate.signatoryList = signatoryList;
    certificate.htmlTemplate = requestData.htmlTemplateUrl;
    certificate.courseName = requestData.courseName;
    certificate.name = requestData.certificateName;
    certificate.description = requestData.certificateDescription;
    return certificate;
  }
  addSignatory() {
    this.signatory.push(this.signatoryCount++)
  }
  dowloadPdf() {
    const requestData = {
      data: {
        params: {},
        request: {
          pdfUrl: this.pdfUrl
        }
      },
      url: urlConfig.URLS.DOWLOAD_PDF
    }
    this.dataService.post(requestData).subscribe(res => {
      console.log(res)
      window.open(res.result.signedUrl, '_blank');
      this.router.navigate(['']);
    });
  }
  removeSignatory() {
    if (this.signatoryCount > 1) {
      this.signatory.pop()
      this.signatoryCount --;
    }
  }

}

