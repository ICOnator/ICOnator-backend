package io.modum.tokenapp.backend.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use {@link org.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version 2.2.1.
 */
public final class ModumToken extends Contract {
    private static final String BINARY = "6060604052629896806005556301c9c3806006556007805460ff1990811690915560786008556009805490911660c81790556000600a55341561004157600080fd5b5b60008054600160a060020a03191633600160a060020a031617905560a06040519081016040908152600080835260208084018290528284018290526060840182905260808401829052600160a060020a03331682526001905220815181556020820151816001015560408201518160020155606082015181600301556080820151600490910155505b5b610dd7806100db6000396000f300606060405236156100d85763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde038114610123578063095ea7b3146101ae57806318160ddd146101e457806323b872dd1461020957806340c10f19146102455780634b9f5c981461027b57806350635394146102a557806370a08231146102cc57806375143ef2146102fd5780638737ecee1461032457806395d89b4114610390578063a9059cbb1461041b578063b0a853de14610451578063dd62ed3e14610478578063e74988d8146104af575b6101215b6000600a5434019050600454818115156100f257fe5b06600a556004546101016104d4565b600a5483030281151561011057fe5b600380549290910490910190555b50565b005b341561012e57600080fd5b6101366104df565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101735780820151818401525b60200161015a565b50505050905090810190601f1680156101a05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156101b957600080fd5b6101d0600160a060020a0360043516602435610516565b604051901515815260200160405180910390f35b34156101ef57600080fd5b6101f76104d4565b60405190815260200160405180910390f35b341561021457600080fd5b6101d0600160a060020a0360043581169060243516604435610583565b604051901515815260200160405180910390f35b341561025057600080fd5b6101d0600160a060020a03600435166024356106a7565b604051901515815260200160405180910390f35b341561028657600080fd5b6101f7600435151561072b565b60405190815260200160405180910390f35b34156102b057600080fd5b6101d06107cd565b604051901515815260200160405180910390f35b34156102d757600080fd5b6101f7600160a060020a0360043516610836565b60405190815260200160405180910390f35b341561030857600080fd5b6101d0610858565b604051901515815260200160405180910390f35b341561032f57600080fd5b6101d060046024813581810190830135806020601f820181900481020160405190810160405281815292919060208401838380828437509496505084359460200135935061088992505050565b604051901515815260200160405180910390f35b341561039b57600080fd5b610136610976565b60405160208082528190810183818151815260200191508051906020019080838360005b838110156101735780820151818401525b60200161015a565b50505050905090810190601f1680156101a05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561042657600080fd5b6101d0600160a060020a03600435166024356109ad565b604051901515815260200160405180910390f35b341561045c57600080fd5b6101d0610abc565b604051901515815260200160405180910390f35b341561048357600080fd5b6101f7600160a060020a0360043581169060243516610b8a565b60405190815260200160405180910390f35b34156104ba57600080fd5b6101f7610bb7565b60405190815260200160405180910390f35b600554600454015b90565b60408051908101604052600b81527f4d6f64756d20546f6b656e000000000000000000000000000000000000000000602082015281565b600160a060020a03338116600081815260026020908152604080832094871680845294909152808220859055909291907f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9259085905190815260200160405180910390a35060015b92915050565b600160a060020a0383166000908152600160205260408120600201548290108015906105d65750600160a060020a0380851660009081526002602090815260408083203390941683529290522054829010155b80156105e25750600082115b801561060a5750600160a060020a038316600090815260016020526040902060020154828101115b1561069b57610617610bbe565b1561062e5761062584610bed565b61062e83610bed565b5b61063884610c42565b61064183610c42565b50600160a060020a038084166000908152600160208181526040808420600290810180548890039055808352818520338716865283528185208054889003905594871684529082905290912090910180548301905561069f565b5060005b5b9392505050565b6000805433600160a060020a039081169116146106c357600080fd5b60075460ff161580156106e15750600654826106dd6104d4565b0111155b15610721576106ef83610c42565b50600160a060020a0382166000908152600160208190526040909120600201805483019055600480548301905561057d565b5060005b92915050565b6000610735610bbe565b151561074057600080fd5b61074933610bed565b600160a060020a0333166000908152600160205260408120600301541161076f57600080fd5b8115156107a857600160a060020a03331660009081526001602052604090206003018054600f8054919091039055546010805490910190555b50600160a060020a03331660009081526001602052604081206003018190555b919050565b6000806107d933610c42565b50600160a060020a0333166000818152600160208190526040808320805493815560035492019190915590919082156108fc0290839051600060405180830381858888f19350505050151561082d57600080fd5b600191505b5090565b600160a060020a0381166000908152600160205260409020600201545b919050565b6000805433600160a060020a0390811691161461087457600080fd5b506007805460ff191660019081179091555b90565b6007546000908190819060ff1615156108a157600080fd5b600d54156108ae57600080fd5b60005433600160a060020a039081169116146108c957600080fd5b600554849010156108d957600080fd5b600084116108e657600080fd5b5050600454600060c060405190810160409081528782526020820187905281018590524260608201526080810183905260a08101829052600b815181908051610933929160200190610cc3565b506020820151600182015560408201518160020155606082015181600301556080820151816004015560a082015160059091015550600192505b50509392505050565b60408051908101604052600381527f4d4f440000000000000000000000000000000000000000000000000000000000602082015281565b600160a060020a0333166000908152600160205260408120600201548290108015906109d95750600082115b8015610a015750600160a060020a038316600090815260016020526040902060020154828101115b15610aad57610a0e610bbe565b15610a2557610a1c33610bed565b610a2583610bed565b5b610a2f33610c42565b610a3883610c42565b600160a060020a0333811660008181526001602052604080822060029081018054889003905593871680835291819020909301805486019055917fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a350600161057d565b50600061057d565b5b92915050565b6000805433600160a060020a03908116911614610ad857600080fd5b600d54600090118015610af05750600854600e540142115b1515610afb57600080fd5b60095460105460649160ff16025b04600b600401541115610b5057600d8054600160a060020a033316600090815260016020526040902060020180549091019055546004805482019055600580549190910390555b600b6000610b5e8282610d42565b506000600182810182905560028301829055600383018290556004830182905560059092015590505b90565b600160a060020a038083166000908152600260209081526040808320938516835292905220545b92915050565b6004545b90565b600080600b60020154118015610bd65750600e544210155b8015610be75750600854600e540142105b90505b90565b600e54600160a060020a038216600090815260016020526040902060040154101561011e57600160a060020a038116600090815260016020526040902060028101546003820155600e546004909101555b5b50565b600160a060020a0381166000908152600160208190526040909120015460035403610c6b6104d4565b600160a060020a0383166000908152600160205260409020600201548202811515610c9257fe5b600160a060020a038416600090815260016020819052604090912080549390920490920181556003549101555b5050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610d0457805160ff1916838001178555610d31565b82800160010185558215610d31579182015b82811115610d31578251825591602001919060010190610d16565b5b50610832929150610d8a565b5090565b50805460018160011615610100020316600290046000825580601f10610d68575061011e565b601f01602090049060005260206000209081019061011e9190610d8a565b5b50565b6104dc91905b808211156108325760008155600101610d90565b5090565b905600a165627a7a723058207a2e42b8696f2035e0ed67a30d3951a5649d4ee37c3e4dbe065bc47a24d297c80029";

    private ModumToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private ModumToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Transfer", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Transfer", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Approval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
            typedResponse._owner = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._spender = (Address) eventValues.getIndexedValues().get(1);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ApprovalEventResponse> approvalEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Approval", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ApprovalEventResponse>() {
            @Override
            public ApprovalEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse._owner = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._spender = (Address) eventValues.getIndexedValues().get(1);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public Future<Utf8String> name() {
        Function function = new Function("name", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> approve(Address _spender, Uint256 _value) {
        Function function = new Function("approve", Arrays.<Type>asList(_spender, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> totalSupply() {
        Function function = new Function("totalSupply", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> transferFrom(Address _from, Address _to, Uint256 _value) {
        Function function = new Function("transferFrom", Arrays.<Type>asList(_from, _to, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> mint(Address _recipient, Uint256 _value) {
        Function function = new Function("mint", Arrays.<Type>asList(_recipient, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> vote(Bool _vote) {
        Function function = new Function("vote", Arrays.<Type>asList(_vote), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> claimBonus() {
        Function function = new Function("claimBonus", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> balanceOf(Address _owner) {
        Function function = new Function("balanceOf", 
                Arrays.<Type>asList(_owner), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> mintFinished() {
        Function function = new Function("mintFinished", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> proposal(Utf8String _addr, Bytes32 _hash, Uint256 _value) {
        Function function = new Function("proposal", Arrays.<Type>asList(_addr, _hash, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Utf8String> symbol() {
        Function function = new Function("symbol", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> transfer(Address _to, Uint256 _value) {
        Function function = new Function("transfer", Arrays.<Type>asList(_to, _value), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<TransactionReceipt> claimProposal() {
        Function function = new Function("claimProposal", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public Future<Uint256> allowance(Address _owner, Address _spender) {
        Function function = new Function("allowance", 
                Arrays.<Type>asList(_owner, _spender), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> getUnlockedTokens() {
        Function function = new Function("getUnlockedTokens", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<ModumToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(ModumToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static Future<ModumToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue) {
        return deployAsync(ModumToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", initialWeiValue);
    }

    public static ModumToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ModumToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ModumToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ModumToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class TransferEventResponse {
        public Address _from;

        public Address _to;

        public Uint256 _value;
    }

    public static class ApprovalEventResponse {
        public Address _owner;

        public Address _spender;

        public Uint256 _value;
    }
}
